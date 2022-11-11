package org.jmol.adapter.writers;

import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import org.jmol.api.JmolWriter;
import org.jmol.modelset.Atom;
import org.jmol.modelset.Bond;
import org.jmol.modelset.LabelToken;
import org.jmol.modelset.Model;
import org.jmol.viewer.Viewer;

import javajs.util.BS;
import javajs.util.Lst;
import javajs.util.OC;
import javajs.util.P3d;
import javajs.util.PT;
import javajs.util.Qd;

/**
 * An XCrysDen XSF writer
 * 
 * see http://www.xcrysden.org/doc/XSF.html
 * 
 */
public class PDBWriter implements JmolWriter {


  private Viewer vwr;
  private OC oc;
  private boolean isPQR;
  private boolean doTransform;
  private boolean allTrajectories;

  public PDBWriter() {
    // for JavaScript dynamic loading
  }

  @Override
  public void set(Viewer viewer, OC oc, Object[] data) {
    vwr = viewer;
    this.oc = (oc == null ? vwr.getOutputChannel(null,  null) : oc);
    isPQR = ((Boolean) data[0]).booleanValue();
    doTransform = ((Boolean) data[1]).booleanValue();
    allTrajectories = ((Boolean) data[2]).booleanValue();
  }

  @Override
  public String write(BS bs) {
    String type = oc.getType();
    isPQR |= (type != null && type.indexOf("PQR") >= 0);
    doTransform |= (type != null && type.indexOf("-coord true") >= 0);
    Atom[] atoms = vwr.ms.at;
    Model[] models = vwr.ms.am;
    String occTemp = "%6.2Q%6.2b          ";
    if (isPQR) {
      occTemp = "%8.4P%7.4V       ";
      double charge = 0;
      for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1))
        charge += atoms[i].getPartialCharge();
      oc.append(
          "REMARK   1 PQR file generated by Jmol " + Viewer.getJmolVersion())
          .append("\nREMARK   1 " + "created " + (new Date()))
          .append("\nREMARK   1 Forcefield Used: unknown\nREMARK   1")
          .append("\nREMARK   5")
          .append("\nREMARK   6 Total charge on this protein: " + charge
              + " e\nREMARK   6\n");
    }

    int iModel = atoms[bs.nextSetBit(0)].mi;
    int iModelLast = -1;
    int lastAtomIndex = bs.length() - 1;
    int lastModelIndex = atoms[lastAtomIndex].mi;
    boolean isMultipleModels = (iModel != lastModelIndex);
    BS bsModels = vwr.ms.getModelBS(bs, true);
    int nModels = bsModels.cardinality();
    Lst<String> lines = new Lst<String>();
    boolean isMultipleBondPDB = models[iModel].isPdbWithMultipleBonds;
    boolean uniqueAtomNumbers = false;
    if (nModels > 1) {
      Object conectArray = null;
      for (int nFiles = 0, i = bsModels.nextSetBit(0); i >= 0; i = bsModels
          .nextSetBit(i + 1)) {
        Object a = vwr.ms.getModelAuxiliaryInfo(i).get("PDB_CONECT_bonds");
        if (a != conectArray || !vwr.ms.am[i].isBioModel) {
          conectArray = a;
          if (nFiles++ > 0) {
            uniqueAtomNumbers = true;
            break;
          }
        }
      }
    }
    LabelToken[] tokens;
    P3d ptTemp = new P3d();
    Object[] o = new Object[] { ptTemp };
    Qd q = (doTransform ? vwr.tm.getRotationQ() : null);
    Map<String, Integer> map = new Hashtable<String, Integer>();
    boolean isBiomodel = false;
    int[] firstAtomIndexNew = (uniqueAtomNumbers ? new int[nModels] : null);
    int modelPt = 0;
    for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
      Atom a = atoms[i];
      isBiomodel = models[a.mi].isBioModel;
      if (isMultipleModels && a.mi != iModelLast) {
        if (iModelLast != -1) {
          modelPt = fixPDBFormat(lines, map, oc, firstAtomIndexNew, modelPt);
          oc.append("ENDMDL\n");
        }
        lines = new Lst<String>();
        iModel = iModelLast = a.mi;
        oc.append("MODEL     " + (iModelLast + 1) + "\n");
      }
      String sa = a.getAtomName();
      boolean leftJustify = (a.getElementSymbol().length() == 2
          || sa.length() >= 4 || PT.isDigit(sa.charAt(0)));
      boolean isHetero = a.isHetero();
      if (!isBiomodel)
        tokens = (leftJustify
            ? LabelToken.compile(vwr,
                "HETATM%5.-5i %-4.4a%1AUNK %1c   1%1E   _XYZ_" + occTemp, '\0',
                null)
            : LabelToken.compile(vwr,
                "HETATM%5.-5i  %-3.3a%1AUNK %1c   1%1E   _XYZ_" + occTemp, '\0',
                null)

        );
      else if (isHetero)
        tokens = (leftJustify
            ? LabelToken.compile(vwr,
                "HETATM%5.-5i %-4.4a%1A%3.3n %1c%4.-4R%1E   _XYZ_" + occTemp,
                '\0', null)
            : LabelToken.compile(vwr,
                "HETATM%5.-5i  %-3.3a%1A%3.3n %1c%4.-4R%1E   _XYZ_" + occTemp,
                '\0', null));
      else
        tokens = (leftJustify
            ? LabelToken.compile(vwr,
                "ATOM  %5.-5i %-4.4a%1A%3.3n %1c%4.-4R%1E   _XYZ_" + occTemp,
                '\0', null)
            : LabelToken.compile(vwr,
                "ATOM  %5.-5i  %-3.3a%1A%3.3n %1c%4.-4R%1E   _XYZ_" + occTemp,
                '\0', null));
      String XX = a.getElementSymbolIso(false).toUpperCase();
      XX = pdbKey(a.group.getBioPolymerIndexInModel())
          + pdbKey(a.group.groupIndex)
          + LabelToken.formatLabelAtomArray(vwr, a, tokens, '\0', null, ptTemp)
          + (XX.length() == 1 ? " " + XX : XX.substring(0, 2)) + "  ";
      vwr.ms.getPointTransf(-1, a, q, ptTemp);
      String xyz = PT.sprintf("%8.3p%8.3p%8.3p", "p", o);
      if (xyz.length() > 24)
        xyz = PT.sprintf("%8.2p%8.2p%8.2p", "p", o);
      XX = PT.rep(XX, "_XYZ_", xyz);
      lines.addLast(XX);
    }
    fixPDBFormat(lines, map, oc, firstAtomIndexNew, modelPt);
    if (isMultipleModels)
      oc.append("ENDMDL\n");

    // now for CONECT records...
    modelPt = -1;
    iModelLast = -1;
    String conectKey = "" + (isMultipleModels ? modelPt : 0);
    isBiomodel = false;
    for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
      Atom a = atoms[i];
      if (a.mi != iModelLast) {
        Model m =  models[a.mi];
        iModelLast = a.mi;
        isBiomodel = m.isBioModel;
        modelPt++;
      }
      boolean isHetero = (!isBiomodel || a.isHetero());
      boolean isCysS = !isHetero && (a.getElementNumber() == 16);
      if (isHetero || isMultipleBondPDB || isCysS) {
        Bond[] bonds = a.bonds;
        if (bonds == null)
          continue;
        for (int j = 0; j < bonds.length; j++) {
          Bond b = bonds[j];
          int iThis = a.getAtomNumber();
          Atom a2 = b.getOtherAtom(a);
          if (!bs.get(a2.i))
            continue;
          int n = b.getCovalentOrder();
          if (n == 1 && (isMultipleBondPDB && !isHetero && !isCysS
              || isCysS && a2.getElementNumber() != 16))
            continue;
          int iOther = a2.getAtomNumber();
          switch (n) {
          case 2:
          case 3:
            if (iOther < iThis)
              continue; // only one entry in this case -- pseudo-PmapDB style
            //$FALL-THROUGH$
          case 1:
            Integer inew = map.get(conectKey + "." + Integer.valueOf(iThis));
            Integer inew2 = map.get(conectKey + "." + Integer.valueOf(iOther));
            if (inew == null || inew2 == null)
              break;
            oc.append("CONECT").append(PT.formatStringS("%5s", "s", "" + inew));
            String s = PT.formatStringS("%5s", "s", "" + inew2);
            for (int k = 0; k < n; k++)
              oc.append(s);
            oc.append("\n");
            break;
          }
        }

      }
    }
    return toString();
  }
  
  private String pdbKey(int np) {
    String xp = (np < 0 ? "~999" : "   " + np); 
    return xp.substring(xp.length() - 4);
  }

  /**
   * must re-order by resno and then renumber atoms and add TER records based on
   * BioPolymers
   * 
   * note: 3hbt has a break between residues 39 and 51 with no TER record, but
   * Jmol will put that in.
   * 
   * @param lines
   * @param map
   * @param out
   * @param modelPt
   * @param firstAtomIndexNew
   * @return new modelPt
   */
  private int fixPDBFormat(Lst<String> lines, Map<String, Integer> map, OC out,
                           int[] firstAtomIndexNew, int modelPt) {
    lines.addLast("~999~999XXXXXX99999999999999999999~99~");
    String[] alines = new String[lines.size()];
    lines.toArray(alines);
    Arrays.sort(alines);
    lines.clear();
    for (int i = 0, n = alines.length; i < n; i++) {
      lines.addLast(alines[i]);
    }
    String lastPoly = null;
    String lastLine = null;
    int n = lines.size();
    int newAtomNumber = 0;
    int iBase = (firstAtomIndexNew == null ? 0 : firstAtomIndexNew[modelPt]);
    for (int i = 0; i < n; i++) {
      String s = lines.get(i);
      String poly = s.substring(0, 4);
      s = s.substring(8);
      boolean isTerm = false;
      boolean isLast = (s.indexOf("~99~") >= 0);
      if (!poly.equals(lastPoly) || isLast) {
        if (lastPoly != null && !lastPoly.equals("~999")) {
          isTerm = true;
          //TER     458      ASN A  78C                                                      
          s = "TER   " + lastLine.substring(6, 11) + "      "
              + lastLine.substring(17, 27);
          lines.add(i, poly + "~~~~" + s);
          n++;
        }
        lastPoly = poly;
      }
      if (isLast && !isTerm)
        break;
      lastLine = s;
      newAtomNumber = i + 1 + iBase;
      if (map != null && !isTerm)
        map.put(
            "" + modelPt + "."
                + Integer.valueOf(PT.parseInt(s.substring(6, 11))),
            Integer.valueOf(newAtomNumber));
      String si = "     " + newAtomNumber;
      out.append(s.substring(0, 6)).append(si.substring(si.length() - 5))
          .append(s.substring(11)).append("\n");
    }
    if (firstAtomIndexNew != null && ++modelPt < firstAtomIndexNew.length)
      firstAtomIndexNew[modelPt] = newAtomNumber;
    return modelPt;
  }



  @Override
  public String toString() {
    return (oc == null ? "" : oc.toString());
  }


}

