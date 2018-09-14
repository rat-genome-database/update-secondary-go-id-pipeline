package edu.mcw.rgd.pipelines;

import java.util.Map;
import java.util.Date;

import edu.mcw.rgd.datamodel.ontology.Annotation;
import edu.mcw.rgd.datamodel.ontologyx.Term;
import edu.mcw.rgd.process.Utils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.FileSystemResource;

/**
 * @author BBakir
 * @since Feb 18, 2008
 * <p>
 * Program to update Secondary GOIDs (TERM_ACC, TERM fields) in the FULL_ANNOT table
 * with the Primary GOIDs (TERM_ACC field) which have the same TERM_ACC with the TERM_ACC field in the ONT_TERMS table.
 * To eliminate UNIQUE CONSTRAINT violation in the FULL_ANNOT table, program deletes the row with Secondary GOID
 * if it has a matching Primary GOID row before it updates.
 */
public class UpdateSecondaryGOID {

    OBOSecondaryIDs obo;
    UpdateSecondaryGoidDAO dao = new UpdateSecondaryGoidDAO();
    private String version;
    private int lastModifiedBy;

    Logger logStatus = Logger.getLogger("status");
    Logger logPairs = Logger.getLogger("pairs");
    Logger logDeleted = Logger.getLogger("deleted");
    Logger logUpdated = Logger.getLogger("updated");

    public static void main(String[] args) throws Exception {

        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(bf).loadBeanDefinitions(new FileSystemResource("properties/AppConfigure.xml"));
        UpdateSecondaryGOID manager = (UpdateSecondaryGOID) (bf.getBean("manager"));
        try {
            manager.run();
        } catch(Exception e) {
            Utils.printStackTrace(e, manager.logStatus);
            throw e;
        }
    }

    void run() throws Exception {

        long time0 = System.currentTimeMillis();

        logStatus.info(this.getVersion());
        logStatus.info(dao.getConnectionInfo());

        String oboFile = getObo().downloadOboFile();

        Map<String, String> secondary2primary = getObo().getSecondaryMappedToPrimaryIDs(oboFile);

        int s=0;
        int updatedTermCount=0;
        int updatedSecondaryGOIDCount=0;
        int deleteDuplicateRowCount=0;

        for (String secondaryID : secondary2primary.keySet()) {
            s++;
            String primaryID = secondary2primary.get(secondaryID);

            for( Annotation secondaryFullAnnot: dao.getAnnotations(secondaryID)) {

                logPairs.info("S: "+secondaryID + " -> P: "+primaryID);
                updatedSecondaryGOIDCount++;

                for( Annotation primaryFullAnnot: dao.getAnnotations(primaryID)) {

                    if( Utils.intsAreEqual(secondaryFullAnnot.getAnnotatedObjectRgdId(), primaryFullAnnot.getAnnotatedObjectRgdId())
                     && Utils.intsAreEqual(secondaryFullAnnot.getRefRgdId(), primaryFullAnnot.getRefRgdId())
                     && Utils.stringsAreEqual(secondaryFullAnnot.getEvidence(), primaryFullAnnot.getEvidence())
                     && Utils.stringsAreEqual(secondaryFullAnnot.getWithInfo(), primaryFullAnnot.getWithInfo())
                     && Utils.intsAreEqual(secondaryFullAnnot.getExpRgdId(), primaryFullAnnot.getExpRgdId()) )
                    {
                        dao.deleteAnnotation(secondaryFullAnnot.getKey());
                        deleteDuplicateRowCount++;
                        logDeleted.info("DEL PrimaryFAKey: "+primaryFullAnnot.getKey()+" SecondaryFAKey: "+secondaryFullAnnot.getKey()+" PrimaryGOID: "+primaryID+" SecondaryGOID: "+secondaryID);
                    }
                }
            }

            for( Annotation secondaryFullAnnot: dao.getAnnotations(secondaryID)) {

                Term term = dao.getTerm(primaryID);
                if( term==null ) {
                    logStatus.warn("CONFLICT: Primary GOID "+primaryID+" not found in RGD database!");
                    continue;
                }

                // update secondary term_acc,term with primary term_acc,term
                secondaryFullAnnot.setTermAcc(primaryID);
                secondaryFullAnnot.setTerm(term.getTerm());
                secondaryFullAnnot.setLastModifiedBy(getLastModifiedBy());
                secondaryFullAnnot.setLastModifiedDate(new Date());
                if( dao.updateAnnotation(secondaryFullAnnot)!=0 ) {

                    logUpdated.info(secondaryID + " -> "+primaryID+" ST: "+secondaryFullAnnot.getTerm()+" PT: "+term.getTerm());
                    updatedTermCount++;
                }
            }

        }

        logStatus.info("Number of secondaryGOIDs read from the file: " + s);
        logStatus.info("Secondary GOIDs are updated in "+updatedSecondaryGOIDCount+" rows in the FULL_ANNOT table");
        logStatus.info(updatedTermCount+" distinct terms are updated for "+updatedSecondaryGOIDCount+" rows in the FULL_ANNOT table");
        logStatus.info(deleteDuplicateRowCount + " duplicate rows are deleted from the FULL_ANNOT table");
        logStatus.info("=== OK == elapsed time "+Utils.formatElapsedTime(System.currentTimeMillis(), time0));
    }

    public OBOSecondaryIDs getObo() {
        return obo;
    }

    public void setObo(OBOSecondaryIDs obo) {
        this.obo = obo;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public void setLastModifiedBy(int lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public int getLastModifiedBy() {
        return lastModifiedBy;
    }
}