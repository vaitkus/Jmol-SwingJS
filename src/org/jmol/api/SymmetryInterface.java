package org.jmol.api;

import java.util.Map;

import org.jmol.modelset.Atom;
import org.jmol.modelset.ModelSet;
import org.jmol.viewer.Viewer;

import javajs.util.BS;
import javajs.util.Lst;
import javajs.util.M3d;
import javajs.util.M4d;
import javajs.util.Matrix;
import javajs.util.P3d;
import javajs.util.Qd;
import javajs.util.SB;
import javajs.util.T3d;
import javajs.util.V3d;

public interface SymmetryInterface {

  int addSpaceGroupOperation(String xyz, int opId);

  void calculateCIPChiralityForAtoms(Viewer vwr, BS bsAtoms);

  String[] calculateCIPChiralityForSmiles(Viewer vwr, String smiles)
      throws Exception;

  int addBioMoleculeOperation(M4d mat, boolean isReverse);

  Object findSpaceGroup(Viewer vwr, BS atoms, String xyzList, double[] unitCellParams, T3d origin, boolean asString, boolean isAssign, boolean checkSupercell);

  int[] getCellRange();

  boolean getCoordinatesAreFractional();

  void getEquivPointList(Lst<P3d> pts, int nIgnore, String flags);

  P3d getFractionalOffset();

  String getIntTableNumber();

  String getIntTableNumberFull();

  int getLatticeOp();

  char getLatticeType();

  Lst<String> getMoreInfo();

  Matrix getOperationRsVs(int op);

  String getPointGroupName();

  Qd getQuaternionRotation(String abc);

  int getSiteMultiplicity(P3d a);

  Object getSpaceGroup();

  Map<String, Object> getSpaceGroupInfo(ModelSet modelSet, String spaceGroup, int modelIndex, boolean isFull, double[] cellParams);

  Object getSpaceGroupInfoObj(String name, double[] params,
                              boolean isFull, boolean addNonstandard);

  String getSpaceGroupName();

  /**
   * 
   * @param type "Hall" or "HM" or "ITA"
   * @return type or null
   */
  String getSpaceGroupNameType(String type);

  M4d getSpaceGroupOperation(int i);
  
  int getSpaceGroupOperationCount();
  
  String getSpaceGroupXyz(int i, boolean doNormalize);

  int getSpinOp(int op);

  boolean getState(ModelSet ms, int modelIndex, SB commands);

  String getSymmetryInfoStr();

  M4d[] getSymmetryOperations();

  M4d getTransform(P3d fracA, P3d fracB, boolean debug);

  SymmetryInterface getUnitCell(T3d[] points, boolean setRelative, String name);

  double[] getUnitCellAsArray(boolean vectorsOnly);

  String getUnitCellInfo(boolean scaled);

  Map<String, Object> getUnitCellInfoMap();

  double getUnitCellInfoType(int infoType);

  SymmetryInterface getUnitCellMultiplied();

  double[] getUnitCellParams();

  String getUnitCellState();

  P3d[] getUnitCellVectors();

  P3d[] getUnitCellVerticesNoOffset();

  T3d[] getV0abc(Object def, M4d m);

  boolean haveUnitCell();

  boolean isBio();

  boolean isPolymer();

  boolean isSimple();

  boolean isSlab();

  boolean isSupercell();

  void newSpaceGroupPoint(P3d pt, int i, M4d o,
                                          int transX, int transY, int transZ, P3d retPoint);

  BS notInCentroid(ModelSet modelSet, BS bsAtoms,
                          int[] minmax);

  BS removeDuplicates(ModelSet ms, BS bs, boolean highPrec);

  V3d[] rotateAxes(int iop, V3d[] axes, P3d ptTemp, M3d mTemp);

  void setFinalOperations(int dim, String name, P3d[] atoms,
                                          int iAtomFirst,
                                          int noSymmetryCount, boolean doNormalize, String filterSymop);

  /**
   * set symmetry lattice type using Hall rotations
   * 
   * @param latt SHELX index or character lattice character P I R F A B C S T or \0
   * 
   */
  void setLattice(int latt);

  void setOffset(int nnn);

  void setOffsetPt(T3d pt);

  void setSpaceGroup(boolean doNormalize);

  void setSpaceGroupName(String name);

  /**
   * 
   * @param spaceGroup ITA number, ITA full name ("48:1")
   */
  void setSpaceGroupTo(Object spaceGroup);

  SymmetryInterface setUnitCellFromParams(double[] params, boolean setRelative, double slop);

  void setUnitCell(SymmetryInterface uc);

  void toCartesian(T3d pt, boolean ignoreOffset);

  void toFractional(T3d pt, boolean ignoreOffset);
  
   boolean toFromPrimitive(boolean toPrimitive, char type, T3d[] oabc,
                          M3d primitiveToCrystal);

  void toUnitCell(T3d pt, T3d offset);

  boolean unitCellEquals(SymmetryInterface uc2);

  void unitize(T3d ptFrac);

  void initializeOrientation(M3d matUnitCellOrientation);

  /**
   * 
   * @param ms
   * @param iatom
   * @param xyz
   * @param op
   * @param translation TODO
   * @param pt
   * @param pt2 a second point or an offset
   * @param id
   * @param type  T.point, T.lattice, or T.draw, T.matrix4f, T.label, T.list, T.info, T.translation, T.axis, T.plane, T.angle, T.center
   * @param scaleFactor
   * @param nth TODO
   * @param options could be T.offset
   * @param oplist 
   * @return a variety of object types
   */
  Object getSymmetryInfoAtom(ModelSet ms, int iatom, String xyz, int op,
                                    P3d translation, P3d pt, P3d pt2, String id, int type, double scaleFactor, int nth, int options, int[] oplist);

  P3d toSupercell(P3d fpt);


  T3d getUnitCellMultiplier();

  P3d getCartesianOffset();

  P3d[] getCanonicalCopy(double scale, boolean withOffset);

  Lst<P3d> getLatticeCentering();

  Object getLatticeDesignation();

  Object getPointGroupInfo(int modelIndex, String drawID,
                           boolean asInfo, String type,
                           int index, double scale);

  SymmetryInterface setPointGroup(
                                  Viewer vwr,
                                  SymmetryInterface pointGroupPrevious, T3d center,
                                  T3d[] atomset,
                                  BS bsAtoms,
                                  boolean haveVibration, double distanceTolerance, double linearTolerance, int maxAtoms, boolean localEnvOnly);

  int[] getInvariantSymops(P3d p3, int[] v0);

  Lst<P3d> getEquivPoints(Lst<P3d> pts, P3d pt, String flags);
  
  Lst<P3d> generateCrystalClass(P3d pt0);

  P3d getFractionalOrigin();

  AtomIndexIterator getIterator(Viewer vwr, Atom atom, BS bstoms, double radius);

  boolean isWithinUnitCell(P3d pt, double x, double y, double z);

  boolean checkPeriodic(P3d pt);

  Object convertOperation(String string, M4d matrix);

  int getAdditionalOperationsCount();

  M4d[] getAdditionalOperations();

  Object getWyckoffPosition(Viewer vwr, P3d pt, String letter);

  Object getSpaceGroupJSON(Viewer vwr, String name, String sgname, int index);

  double getCellWeight(P3d pt);

  double getPrecision();

  boolean fixUnitCell(double[] unitCellParams);

  String getSpaceGroupTitle();

  boolean isSymmetryCell(SymmetryInterface sym);
}
