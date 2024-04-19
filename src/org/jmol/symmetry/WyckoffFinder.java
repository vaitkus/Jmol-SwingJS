package org.jmol.symmetry;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;

import org.jmol.viewer.FileManager;
import org.jmol.viewer.Viewer;

import javajs.util.JSJSONParser;
import javajs.util.Lst;
import javajs.util.M4d;
import javajs.util.MeasureD;
import javajs.util.P3d;
import javajs.util.P4d;
import javajs.util.PT;
import javajs.util.Rdr;
import javajs.util.SB;
import javajs.util.V3d;

/**
 * A class to calculate, store, and retrieve Wyckoff information as per
 * BCS nph-trgen at
 * 
 * https://www.cryst.ehu.es/cgi-bin/cryst/programs//nph-trgen?gnum=146&what=wpos&trmat=2/3a+1/3b+1/3c,-1/3a+1/3b+1/3c,-1/3a-2/3b+1/3c&unconv=R%203%20:R&from=ita
 * 
 * For the 611 standard setting in ITA GENPOS, we are just reading the json file and 
 * loading its information.
 * 
 * For setting not found through GENPOS, we calculate the Wyckoff positions. 
 *  
 *   
 * 
 */
public class WyckoffFinder {

  private static WyckoffFinder nullHelper;
  private final static Map<String, WyckoffFinder> helpers = new Hashtable<String, WyckoffFinder>();

  /**
   * positive numbers will be label characters or '*'
   */
  public final static int WYCKOFF_RET_LABEL = -1;
  public final static int WYCKOFF_RET_COORD = -2;
  public final static int WYCKOFF_RET_COORDS = -3;
  public final static int WYCKOFF_RET_COORDS_ALL = '*';
  public final static int WYCKOFF_RET_GENERAL = 'G';
  public final static int WYCKOFF_RET_CENTERING = 'C';
  public final static int WYCKOFF_RET_CENTERING_STRING = 'S';
  public final static int WYCKOFF_RET_WITH_MULT = 'M';

  private Lst<Object> positions;
  private int npos, ncent;
  protected P3d[] centerings;
  protected String[] centeringStr;
  private Lst<Object> gpos;

  public WyckoffFinder() {
    // only used for dynamic instantiation from Symmetry.java
  }

  /**
   * Retrieve the JSON data for this space group and extract its Wyckoff
   * information.
   * 
   * Effectively static, as this is only accessed from the singleton static
   * helper instance. But we leave it not static so as not to generate a "static
   * access" warning.
   * 
   * @param vwr
   * @param sg
   * @return helper
   */
  WyckoffFinder getWyckoffFinder(Viewer vwr, SpaceGroup sg) {
    WyckoffFinder helper = helpers.get(sg.clegId);
    if (helper == null) {
      helper = createHelper(this, vwr, sg);
    }
    if (helper == null) {
      if (nullHelper == null)
        nullHelper = new WyckoffFinder(null);
      helpers.put(sg.clegId, nullHelper);
    }
    return helper;
  }

  /**
   * Given a fractional coordinate and a Wyckoff position letter such as "b",
   * determine the matching orbit symmetry element and project p onto it.
   * 
   * @param p
   * @param letter
   * @return p as its projection
   */
  P3d findPositionFor(P3d p, String letter) {
    if (positions != null) {
      boolean isGeneral = (letter.equals("G"));
      for (int i = isGeneral ? 1 : npos; --i >= 0;) {
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) positions.get(i);
        String l = (String) map.get("label");
        if (isGeneral || l.equals(letter)) {
          @SuppressWarnings("unchecked")
          Lst<Object> coords = (Lst<Object>) map.get("coord");
          if (coords != null)
            getWyckoffCoord(coords, 0, l).project(p);
          return p;
        }
      }
    }
    return null;
  }

  /**
   * Get string information about this position or space group
   * 
   * @param uc
   * @param p
   * @param returnType
   *        label, coord, label*, or
   * @param withMult
   * @return label or coordinate or label with centerings and coordinates or
   *         full list for space group
   */
  Object getInfo(UnitCell uc, P3d p, int returnType, boolean withMult) {
    Object info = createInfo(uc, p, returnType, withMult);
    return (info == null ? "?" : info);
  }

  /**
   * Convert "1/2,1/2,0" to {0.5 0.5 0}
   * 
   * @param xyz
   * @param p
   * @return p or new P3d()
   */
  static P3d toPoint(String xyz, P3d p) {
    if (p == null)
      p = new P3d();
    String[] s = PT.split(xyz, ",");
    p.set(PT.parseDoubleFraction(s[0]), PT.parseDoubleFraction(s[1]),
        PT.parseDoubleFraction(s[2]));
    return p;
  }

  /**
   * Just wrap the coordinate with parentheses.
   * 
   * @param xyz
   * @param sb
   * @return sb for continued appending
   */
  protected static SB wrap(String xyz, SB sb) {
    return sb.appendC('(').append(xyz).appendC(')');
  }

  /**
   * Generate information for the symop("wyckoff") script function
   * 
   * @param uc
   * @param p
   * @param returnType
   *        '*', -1, -2, -3, or a character label 'a'-'A' or 'G' for general or
   *        'C' for centerings
   * @param withMult
   *        from "wyckoffm"
   * @return an informational string
   */
  @SuppressWarnings("unchecked")
  private Object createInfo(UnitCell uc, P3d p, int returnType,
                            boolean withMult) {
    switch (returnType) {
    case WYCKOFF_RET_CENTERING_STRING:
      return getCenteringStr(-1, ' ', null).toString().trim();
    case WYCKOFF_RET_CENTERING:
      P3d[] ret = new P3d[centerings.length];
      for (int i = ret.length; --i >= 0;)
        ret[i] = centerings[i];
      return ret;
    case WYCKOFF_RET_COORDS_ALL:
      SB sb = new SB();
      getCenteringStr(-1, '+', sb);
      for (int i = npos; --i >= 0;) {
        Map<String, Object> map = (Map<String, Object>) positions.get(i);
        String label = (withMult ? "" + map.get("mult") : "")
            + map.get("label");
        sb.appendC('\n').append(label);
        getList(i == 0 ? gpos : (Lst<Object>) map.get("coord"), label, sb,
            (i == 0 ? ncent : 0));
      }
      return sb.toString();
    case WYCKOFF_RET_LABEL:
    case WYCKOFF_RET_COORD:
    case WYCKOFF_RET_COORDS:
      for (int i = npos; --i >= 0;) {
        Map<String, Object> map = (Map<String, Object>) positions.get(i);
        String label = (withMult ? "" + map.get("mult") : "")
            + map.get("label");
        if (i == 0) {
          // general
          switch (returnType) {
          case WYCKOFF_RET_LABEL:
            return label;
          case WYCKOFF_RET_COORD:
            return "(x,y,z)";
          case WYCKOFF_RET_COORDS:
            SB sbc = new SB();
            sbc.append(label).appendC(' ');
            getCenteringStr(-1, '+', sbc).appendC(' ');
            getList(gpos, label, sbc, ncent);
            return sbc.toString();
          }
        }
        Lst<Object> coords = (Lst<Object>) map.get("coord");
        for (int c = 0, n = coords.size(); c < n; c++) {
          WyckoffCoord coord = getWyckoffCoord(coords, c, label);
          //          System.out.println(label+ " " + coord + " " +  c + " " + p);
          if (coord.contains(this, uc, p)) {
            switch (returnType) {
            case WYCKOFF_RET_LABEL:
              return label;
            case WYCKOFF_RET_COORD:
              return coord.asString(null, true).toString();
            case WYCKOFF_RET_COORDS:
              SB sbc = new SB();
              sbc.append(label).appendC(' ');
              getCenteringStr(-1, '+', sbc).appendC(' ');
              getList(coords, label, sbc, 0);
              return sbc.toString();
            }
          }
        }
      }
      break;
    case WYCKOFF_RET_GENERAL:
    default:
      // specific letter
      String letter = "" + (char) returnType;
      boolean isGeneral = (returnType == WYCKOFF_RET_GENERAL);
      P3d tempP = new P3d();
      for (int i = isGeneral ? 1 : npos; --i >= 0;) {
        Map<String, Object> map = (Map<String, Object>) positions.get(i);
        String label = (String) map.get("label");
        if (isGeneral || label.equals(letter)) {
          SB sbc = new SB();
          if (isGeneral)
            sbc.append(label).appendC(' ');
          Lst<Object> coords = (i == 0 ? gpos : (Lst<Object>) map.get("coord"));
          getList(coords, (withMult ? map.get("mult") : "") + letter, sbc, 0);
          if (i > 0 && ncent > 0) {
            M4d tempOp = new M4d();
            for (int j = 0; j < ncent; j++) {
              addCentering(coords, centerings[j], tempOp, tempP, sbc);
            }
          }
          return sbc.toString();
        }
      }
      break;
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private static WyckoffFinder createHelper(Object w, Viewer vwr,
                                            SpaceGroup sg) {
    String sgname = sg.clegId;
    int pt = sgname.indexOf(":");
    int itno = PT.parseInt(sgname.substring(0, pt));
    if (itno >= 1 && itno <= 230) {
      Map<String, Object> resource = getResource(w, vwr,
          "ita_" + itno + ".json");
      if (resource != null) {
        Lst<Object> its = (Lst<Object>) resource.get("its");
        Map<String, Object> map = null;
        boolean haveMap = false;
        for (int i = 0, c = its.size(); i < c; i++) {
          map = (Map<String, Object>) its.get(i);
          if (sgname.equals(map.get("clegId"))) {
            haveMap = true;
            break;
          }
        }
        // "more" type, from wp-list, does note contain gp or wpos
        if (!haveMap || map.containsKey("wpos"))
          map = createWyckoffData(sg, itno, (Map<String, Object>) its.get(0));
        WyckoffFinder helper = new WyckoffFinder(map);
        helpers.put(sgname, helper);
        return helper;

      }
    }
    return null;
  }

  /**
   * Create a Wyckoff information map for a setting that is not in the ITA (at
   * leaset not in the General Position list.)
   * 
   * Check is that 100:a+b,a-b,c comes out right.
   * 
   * The task is three-fold:
   * 
   * 1) find the centering
   * 
   * 2) apply the operations
   * 
   * 3) update the multiplicities
   * 
   * @param sg
   * @param itno
   * @param its0
   * @return new map containing just gp and wpos
   */
  @SuppressWarnings("unchecked")
  private static Map<String, Object> createWyckoffData(SpaceGroup sg, int itno,
                                                       Map<String, Object> its0) {
    M4d trm = UnitCell.toTrm(sg.itaTransform, null);
    Lst<Object> gp0 = (Lst<Object>) its0.get("gp");
    Map<String, Object> wpos0 = (Map<String, Object>) its0.get("wpos");
    Lst<Object> cent0 = (Lst<Object>) wpos0.get("cent");
    Lst<Object> cent = new Lst<>();
    if (cent0 != null)
      cent.addAll(cent0);
    int nctr0 = cent.size();
    M4d trmInv = getTransformedCentering(trm, cent);
    int nctr = cent.size();
    Lst<Object> pos0 = (Lst<Object>) wpos0.get("pos");
    Lst<Object> pos = new Lst<Object>();
    M4d t = new M4d();
    double[] v = new double[16];
    double f = (nctr + 1d) / (nctr0 + 1);
    for (int i = 0, n = pos0.size(); i < n; i++) {
      Map<String, Object> p0 = (Map<String, Object>) pos0.get(i);
      Map<String, Object> p = new Hashtable<String, Object>();
      p.putAll(p0);
      Lst<Object> coord = (Lst<Object>) p0.get("coord");
      if (coord != null) {
        coord = transformCoords(coord, trmInv, null, t, v, null);
        p.put("coord", coord);
      }
      int mult = ((Integer) p0.get("mult")).intValue();
      p.put("mult", Integer.valueOf((int) (mult * f)));
      pos.addLast(p);
    }
    Lst<Object> gp = new Lst<Object>();
    transformCoords(gp0, trmInv, null, t, v, gp);
    if (nctr > 0) {
      for (int i = 0; i < nctr; i++) {
        P3d p = new P3d();
        transformCoords(gp0, trmInv, toPoint((String) cent.get(i), p), t,
            v, gp);
      }
    }
    Map<String, Object> its = new Hashtable<String, Object>();
    Map<String, Object> wpos = new Hashtable<>();
    if (nctr > 0)
      wpos.put("cent", cent);
    wpos.put("pos", pos);
    its.put("gp", gp);
    its.put("wpos", wpos);

    return its;
  }

  private static Lst<Object> transformCoords(Lst<Object> coord, M4d trmInv, P3d centering, M4d t,
                                             double[] v, Lst<Object> coordt) {
    if (coordt == null)
      coordt = new Lst<>();
    for (int j = 0, n = coord.size(); j < n; j++) {
      coordt.addLast(SymmetryOperation.transformStr((String) coord.get(j), null,
          trmInv, t, v, null, centering, true, true));
    }
    return coordt;
  }

  /**
   * adjust centering based on transformation
   * 
   * @param trm
   * @param cent list to be revised
   * @return trmInv
   */
  private static M4d getTransformedCentering(M4d trm, Lst<Object> cent) {
    M4d trmInv = M4d.newM4(trm);
    trmInv.invert();
    int n0 = cent.size();
    P3d p = new P3d();
    double[][] c = UnitCell.getTransformRange(trm);
    if (c != null) {
      for (int i = (int) c[0][0]; i < c[1][0]; i++) {
        for (int j = (int) c[0][1]; j <= c[1][1]; j++) {
          for (int k = (int) c[0][2]; k <= c[1][2]; k++) {
            p.set(i, j, k);
            trmInv.rotTrans(p);
            if (p.length() % 1 != 0) {
              p.x = p.x % 1;
              p.y = p.y % 1;
              p.z = p.z % 1;
              String s = SymmetryOperation.norm3(p);
              if (!s.equals("0,0,0") && !cent.contains(s))
                cent.addLast(s);
            }

          }
        }
      }
      int n = cent.size();
      if (n > 0) {
        String[] a = new String[n];
        cent.toArray(a);
        Arrays.sort(a);
        cent.clear();
        for (int i = 0; i < n; i++)
          cent.addLast(a[i]);
      }
    }
    // remove integral centerings -- when det < 1
    for (int i = n0; --i >= 0;) {
      toPoint((String) cent.get(i), p);
      trmInv.rotTrans(p);
      if (p.x % 1 == 0 && p.y % 1 == 0 && p.z % 1 == 0)
        cent.remove(i);
    }
    return trmInv;
  }

  /**
   * Load data from the JSON map.
   * 
   * @param map
   */
  @SuppressWarnings("unchecked")
  private WyckoffFinder(Map<String, Object> map) {
    if (map != null) {
      gpos = (Lst<Object>) map.get("gp");
      Map<String, Object> wpos = (Map<String, Object>) map.get("wpos");
      positions = (Lst<Object>) wpos.get("pos");
      npos = positions.size();
      Lst<Object> cent = (Lst<Object>) wpos.get("cent");
      if (cent != null) {
        ncent = cent.size();
        centeringStr = new String[ncent];
        centerings = new P3d[ncent];
        for (int i = ncent; --i >= 0;) {
          String s = (String) cent.get(i);
          centeringStr[i] = s;
          centerings[i] = toPoint(s, null);
        }
      }
    }
  }

  private SB getCenteringStr(int index, char sep, SB sb) {
    if (sb == null)
      sb = new SB();
    if (ncent == 0)
      return sb;
    if (index >= 0) {
      sb.appendC(sep);
      return wrap(centeringStr[index], sb);
    }
    for (int i = 0; i < ncent; i++) {
      sb.appendC(sep);
      wrap(centeringStr[i], sb);
    }
    return sb;
  }

  private static SB getList(Lst<Object> coords, String letter, SB sb, int n) {
    if (sb == null)
      sb = new SB();
    n = (n == 0 ? coords.size() : coords.size() / (n + 1));
    for (int c = 0; c < n; c++) {
      WyckoffCoord coord = getWyckoffCoord(coords, c, letter);
      sb.append(" ");
      coord.asString(sb, false);
    }
    return sb;
  }

  private void addCentering(Lst<Object> coords, P3d centering, M4d tempOp,
                            P3d tempP, SB sb) {
    for (int n = coords.size(), c = 0; c < n; c++) {
      WyckoffCoord coord = (WyckoffCoord) coords.get(c);
      sb.append(" ");
      coord.asStringCentered(centering, tempOp, tempP, sb);
    }
  }

  private static WyckoffCoord getWyckoffCoord(Lst<Object> coords, int c,
                                              String label) {
    Object coord = coords.get(c);
    if (coord instanceof String) {
      coords.set(c, coord = new WyckoffCoord((String) coord, label));
    }
    return (WyckoffCoord) coord;
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> getResource(Object w, Viewer vwr,
                                                 String resource) {
    try {
      BufferedReader r = FileManager.getBufferedReaderForResource(vwr, w,
          "org/jmol/symmetry/", "sg/json/" + resource);
      String[] data = new String[1];
      if (Rdr.readAllAsString(r, Integer.MAX_VALUE, false, data, 0)) {
        return (Map<String, Object>) new JSJSONParser().parse(data[0], true);
      }
    } catch (Throwable e) {
      System.err.println(e.getMessage());
    }
    return null;
  }

  /**
   * The WyckoffCoord class.
   * 
   * A coordinate in the orbit of a Wyckoff position.
   *
   * Defined by a symmetry element (point, line, or plane), and an associated
   * 4x4 matrix representation.
   * 
   * The matrix operation is used to bring general positions into its orbit.
   * This matrix is used for lines and planes to define the symmetry element
   * associated with the position.
   * 
   * A point if of this position coordinate if it not claimed by a Wyckoff
   * position of higher priority (points before lines, for example) by checking
   * its distance to the symmetry element -- whether it is on the line or in the
   * plane.
   * 
   * 
   * This static class has only three relatively "public" methods (accessed only
   * by WyckoffFinder):
   *
   * 
   * protected boolean contains(WyckoffFinder w, UnitCell uc, P3d p)
   * 
   * protected SB asString(SB sb, boolean withCentering)
   *
   * protected void project(P3d p)
   *
   *
   * The first of these checks to see if a given point is of this coordinate
   * type. It does this by checking a 5x5 range of unit cell offsets around the
   * basic unit cell. This was found to be necessary for some of the hexagonal
   * space groups.
   * 
   * The second returns a parentheses-wrapped string, with added centering if
   * appropriate. The third projects a general position onto this particular
   * orbit coordinate. Jmol uses this to generate a starting position for
   * 
   * MODELKIT ADD C WYCKOFF x
   * 
   * 
   */
  static class WyckoffCoord {

    private final static int TYPE_POINT = 1;
    private final static int TYPE_LINE = 2;
    private final static int TYPE_PLANE = 3;

    /**
     * type of this position; point, line or plane
     */
    private int type;

    /**
     * string representation, for example: "2x,x,11/12"
     */
    private String xyz;

    private String label;

    /**
     * centering discovered to match a point to this position. Only temporary;
     * cleared after first retrieval.
     */
    private transient String thisCentering = "";

    /**
     * 4x4 matrix representation of this position coordinate
     */
    private M4d op;

    /**
     * symmetry element associated with this coordinate.
     * 
     */
    private P3d point;
    private V3d line;
    private P4d plane;

    /**
     * static temporary variables
     */
    private final static P3d p1 = new P3d(), p2 = new P3d(), p3 = new P3d(),
        pc = new P3d();
    private final static V3d vt = new V3d();

    /**
     * Create and initialize a position coordinate.
     * 
     * @param xyz
     *        "1/8,-y+1/8,y+1/8"
     * @param label
     *        "a", "b", etc.
     */
    WyckoffCoord(String xyz, String label) {
      this.xyz = xyz;
      this.label = label;
      create(xyz);
    }

    public void asStringCentered(P3d centering, M4d tempOp, P3d tempP, SB sb) {
      tempOp.setM4(op);
      tempOp.add(centering);
      tempOp.getTranslation(tempP);
      tempP.x = tempP.x % 1;
      tempP.y = tempP.y % 1;
      tempP.z = tempP.z % 1;
      tempOp.setTranslation(tempP);
      sb.appendC(' ');
      String s = "," + SymmetryOperation.getXYZFromMatrixFrac(tempOp, false,
          true, false, true) + ",";
      s = PT.rep(s, ",,", ",0,");
      s = PT.rep(s, ",+", ",");
      sb.appendC('(').append(s.substring(1, s.length() - 1)).appendC(')');
    }

    /**
     * Check to see if the given point is associated with this Wyckoff position.
     * 
     * @param w
     * @param uc
     * @param p
     * 
     * @return true if claimed
     */
    protected boolean contains(WyckoffFinder w, UnitCell uc, P3d p) {
      double slop = uc.getPrecision();
      thisCentering = null;
      // do a preliminary check
      if (checkLatticePt(p, slop))
        return true;
      if (w.centerings == null)
        return false;
      for (int i = w.centerings.length; --i >= 0;) {
        pc.add2(p, w.centerings[i]);
        uc.unitize(pc);
        if (checkLatticePt(pc, slop)) {
          thisCentering = w.centeringStr[i];
          return true;
        }
      }
      return false;
    }

    /**
     * Project a general position onto this Wyckoff position.
     * 
     * @param p
     */
    protected void project(P3d p) {
      switch (type) {
      case TYPE_POINT:
        p.setT(point);
        break;
      case TYPE_LINE:
        MeasureD.projectOntoAxis(p, point, line, vt);
        break;
      case TYPE_PLANE:
        MeasureD.getPlaneProjection(p, plane, vt, vt);
        p.setT(vt);
        break;
      }
    }

    /**
     * Return the parentheses-wrapped string form of this position, possibly
     * with added centering.
     * 
     * @param sb
     * @param withCentering
     * @return sb for continued appending
     */
    protected SB asString(SB sb, boolean withCentering) {
      if (sb == null)
        sb = new SB();
      wrap(xyz, sb);
      if (withCentering && thisCentering != null) {
        sb.appendC('+');
        wrap(thisCentering, sb);
      }
      return sb;
    }

    /**
     * Thoroughly check a 3x3 range of lattice offsets. This is important for
     * groups such as 178 that have oddly directed angles.
     * 
     * Checks 000 first.
     * 
     * @param p
     * @param slop
     *        set false if 000 has been done first
     * @return true if found
     */
    private boolean checkLatticePt(P3d p, double slop) {
      if (checkPoint(p, slop))
        return true;
      for (int z = 125 / 2, i = -2; i < 3; i++) {
        for (int j = -2; j < 3; j++) {
          for (int k = -2; k < 3; k++, z--) {
            if (z == 0)
              continue;
            p3.set(i, j, k);
            p3.add(p);
            if (checkPoint(p3, slop)) {
              System.out.println(
                  label + " " + xyz + " found for " + i + " " + j + " " + k);
              return true;
            }
          }
        }
      }
      return false;
    }

    /**
     * Checks a point by measuring its distance to this coordinate's symmetry
     * element.
     * 
     * @param p
     * @param slop
     * @return true if the point is within slop fractional distance to the
     *         element.
     */
    private boolean checkPoint(P3d p, double slop) {
      double d = 1;
      switch (type) {
      case TYPE_POINT:
        // will be unitized
        d = point.distance(p);
        break;
      case TYPE_LINE:
        p1.setT(p);
        MeasureD.projectOntoAxis(p1, point, line, vt);
        d = p1.distance(p);
        break;
      case TYPE_PLANE:
        d = Math.abs(MeasureD.getPlaneProjection(p, plane, vt, vt));
        break;
      }
      return d < slop;
    }

    /**
     * Generate the operator matrix and the symmetry elements associated with
     * this coordinate.
     * 
     * @param p
     */
    private void create(String p) {
      int nxyz = (p.indexOf('x') >= 0 ? 1 : 0) + (p.indexOf('y') >= 0 ? 1 : 0)
          + (p.indexOf('z') >= 0 ? 1 : 0);
      double[] a = new double[16];
      String[] v = PT.split(xyz, ",");
      getRow(v[0], a, 0);
      getRow(v[1], a, 4);
      getRow(v[2], a, 8);
      a[15] = 1;
      op = M4d.newA16(a);
      switch (nxyz) {
      case 0:
        type = TYPE_POINT;
        point = toPoint(p, null);
        break;
      case 1:
        // just one 
        type = TYPE_LINE;
        p1.set(0.19d, 0.53d, 0.71d);
        op.rotTrans(p1);
        p2.set(0.51d, 0.27d, 0.64d);
        op.rotTrans(p2);
        p2.sub2(p2, p1);
        p2.normalize();
        point = P3d.newP(p1);
        line = V3d.newV(p2);
        break;
      case 2:
        type = TYPE_PLANE;
        p1.set(0.19d, 0.51d, 0.73d);
        op.rotTrans(p1);
        p2.set(0.23d, 0.47d, 0.86d);
        op.rotTrans(p2);
        p3.set(0.1d, 0.2d, 0.3d);
        op.rotTrans(p3);
        plane = MeasureD.getPlaneThroughPoints(p1, p2, p3, null, null,
            new P4d());
        break;
      case 3:
        // general position
        break;
      }
    }

    /**
     * Fill out a row in the op matrix based on a part of the coordiante string
     * 
     * @param s
     *        "2x-y,y+x,11/12"
     * @param a
     * @param rowpt
     */
    private static void getRow(String s, double[] a, int rowpt) {
      // -x+1/2 => +-x+1/2 => ["","-x","1/2"] 
      // 2x+-3  =>  ["2*x","-3"]
      // 2/3x => ["2/3x"]
      // x-y =>  ["x","-y"]
      s = PT.rep(s, "-", "+-");
      s = PT.rep(s, "x", "*x");
      s = PT.rep(s, "y", "*y");
      s = PT.rep(s, "z", "*z");
      s = PT.rep(s, "-*", "-");
      s = PT.rep(s, "+*", "+");
      String[] part = PT.split(s, "+");
      for (int p = part.length; --p >= 0;) {
        s = part[p];
        if (s.length() == 0)
          continue;
        int pt = 3;
        if (s.indexOf('.') >= 0) {
          double d = PT.parseDouble(s);
          a[rowpt + pt] = d;
          continue;
        }
        int i0 = 0;
        double sgn = 1;
        switch (s.charAt(0)) {
        case '-':
          sgn = -1;
          //$FALL-THROUGH$
        case '*':
          i0++;
          break;
        }
        double v = 0;
        // reverse-scanning wins the day
        for (int i = s.length(), f2 = 0; --i >= i0;) {
          char c = s.charAt(i);
          switch (c) {
          case 'x':
            pt = 0;
            v = 1;
            break;
          case 'y':
            pt = 1;
            v = 1;
            break;
          case 'z':
            pt = 2;
            v = 1;
            break;
          case '/':
            f2 = 1;
            v = 1 / v;
            //$FALL-THROUGH$
          case '*':
            sgn *= v;
            v = 0;
            break;
          default:
            int u = "0123456789".indexOf(c);
            if (u < 0)
              System.err.println("WH ????");
            if (v == 0) {
              v = u;
            } else {
              f2 = (f2 == 0 ? 10 : f2 * 10);
              v += f2 * u;
            }
            break;
          }
        }
        a[rowpt + pt] = sgn * v;
      }
    }

    @Override
    public String toString() {
      return asString(null, false).toString();
    }
  } // end of WyckoffCoord

  /*  Jmol script Test:
  
      function testw(aflowid) {
        if (!aflowid)
          aflowid = "225.1";
        var f = "=aflowlib/" + aflowid; 
  
        // aflow file data
        load @f packed
        print {*}.wyckoff.pivot;
  
        // just by site in the CIF file
        load "" packed filter "nowyckoff" 
        print {*}.wyckoff.pivot; 
  
        // all atoms
        ar = [];
        for (a in all){ ar.push(a.symop("wyckoff")); }
        print ar.pivot;
      }
  
      testw("225.1")
      testw("178.1")
    
  */

}
