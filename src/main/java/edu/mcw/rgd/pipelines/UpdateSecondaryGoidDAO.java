package edu.mcw.rgd.pipelines;

import edu.mcw.rgd.dao.impl.AnnotationDAO;
import edu.mcw.rgd.dao.impl.OntologyXDAO;
import edu.mcw.rgd.datamodel.ontology.Annotation;
import edu.mcw.rgd.datamodel.ontologyx.Term;
import edu.mcw.rgd.datamodel.ontologyx.TermSynonym;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mtutaj
 * @since May 16, 2011
 */
public class UpdateSecondaryGoidDAO {

    AnnotationDAO annotDAO = new AnnotationDAO();
    OntologyXDAO ontDAO = new OntologyXDAO();

    public String getConnectionInfo() {
        return annotDAO.getConnectionInfo();
    }

    /**
     * wrapper: getannotation for given term acc id
     * @param accId term accession id
     * @return list of Annotation objects (rows of FULL_ANNOT table)
     * @throws Exception
     */
    public List<Annotation> getAnnotations(String accId) throws Exception {

        return annotDAO.getAnnotations(accId, false, 0);
    }

    /**
     * update annotation object
     *
     * @param annot Annotation object to be updated; key must be set
     * @return number of rows affected by the update
     * @throws Exception on spring framework dao failure
     */
    public int updateAnnotation(Annotation annot) throws Exception {
        return annotDAO.updateAnnotation(annot);
    }

    /**
     * delete annotation object given full_annot_key
     *
     * @param fullAnnotKey full_annot.full_annot_key
     * @return number of rows affected by the delete: 1 - successful delete, 0 - invalid key
     * @throws Exception on spring framework dao failure
     */
    public int deleteAnnotation(int fullAnnotKey) throws Exception {
        return annotDAO.deleteAnnotation(fullAnnotKey);
    }

    /**
     * get an ontology term given term accession id;
     * return null if accession id is invalid
     * @param accId term accession id
     * @return Term object if given term found in database or null otherwise
     * @throws Exception if something wrong happens in spring framework
     */
    public Term getTerm(String accId) throws Exception {
        return ontDAO.getTermByAccId(accId);
    }

    public Map<String,String> getSecondary2PrimaryMap() throws Exception {
        Map<String,String> resultMap = new HashMap<>();
        String[] ontologies = { "BP", "CC", "MF" }; // GO ontologies
        for( String ontId: ontologies ) {
            List<TermSynonym> altIdSynonyms = ontDAO.getActiveSynonymsByType(ontId, "alt_id");
            for( TermSynonym tsyn: altIdSynonyms ) {
                if( tsyn.getName().startsWith("GO:") ) {
                    resultMap.put(tsyn.getName(), tsyn.getTermAcc());
                }
            }
        }
        return resultMap;
    }
}
