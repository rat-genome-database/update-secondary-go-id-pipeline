package edu.mcw.rgd.pipelines;

import edu.mcw.rgd.process.FileDownloader;
import edu.mcw.rgd.process.Utils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.FileSystemResource;

import java.io.*;
import java.util.*;

/**
 * @author mtutaj
 * @since Mar 4, 2014
 * extract secondary ids (lines starting with alt_id:) from all terms in the obo file
 * @deprecated replaced by retrieving data directly from database
 */
public class OBOSecondaryIDs {

    private String externalOboFile;
    private String localOboFile;

    public Map<String, Set<String>> getPrimaryWithSecondaryIDs(String oboFile) throws Exception {

        Map<String, Set<String>> primaries = new HashMap<String, Set<String>>();

        BufferedReader fi = Utils.openReader(oboFile);

        String line;
        String id = null;
        Set<String> altIds = new HashSet<String>();
        while( (line=fi.readLine())!=null ) {
            if( line.startsWith("id:") ) {
                // new id, flush old data
                if( !altIds.isEmpty() ) {
                    primaries.put(id, altIds);
                    altIds = new HashSet<String>();
                }
                id = line.substring(3).trim();
                continue;
            }
            if( line.startsWith("alt_id:") ) {
                altIds.add(line.substring(7).trim());
            }
        }

        fi.close();
        return primaries;
    }

    public Map<String, String> getSecondaryMappedToPrimaryIDs(String oboFile) throws Exception {
        Map<String, Set<String>> primaries = getPrimaryWithSecondaryIDs(oboFile);
        Map<String,String> secondary2primary = new HashMap<String, String>();
        for (String primaryID : primaries.keySet()) {
            // May have multiple secondary ids
            // so we need to iterate over the set
            for (String secondaryID : primaries.get(primaryID)) {
                secondary2primary.put(secondaryID, primaryID);
            }
        }
        return secondary2primary;
    }

    public static void main(String[] args) throws Exception {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(bf).loadBeanDefinitions(new FileSystemResource("properties/AppConfigure.xml"));
        OBOSecondaryIDs sids = (OBOSecondaryIDs) (bf.getBean("obo"));

        String oboFile = sids.downloadOboFile();
        Map<String, Set<String>> primary2secondaries = sids.getPrimaryWithSecondaryIDs(oboFile);
        Map<String, String> secondary2primary = sids.getSecondaryMappedToPrimaryIDs(oboFile);

        for (String id : primary2secondaries.keySet()) {
            Set<String> secondaryIDs = primary2secondaries.get(id);
            System.out.print(id + " - ");
            System.out.println(secondaryIDs);
        }

        for (String secondaryID : secondary2primary.keySet()) {
            System.out.println("Secondary ID: " + secondaryID + " is now: " + secondary2primary.get(secondaryID));
        }


    }

    /** download obo file and store it locally, gzip compressed;
     * return the local file name
     */
    public String downloadOboFile() throws Exception {

        FileDownloader downloader = new FileDownloader();
        downloader.setExternalFile(getExternalOboFile());
        downloader.setLocalFile(getLocalOboFile());
        downloader.setPrependDateStamp(true);
        downloader.setUseCompression(true);
        return downloader.downloadNew();
    }

    public void setExternalOboFile(String externalOboFile) {
        this.externalOboFile = externalOboFile;
    }

    public String getExternalOboFile() {
        return externalOboFile;
    }

    public void setLocalOboFile(String localOboFile) {
        this.localOboFile = localOboFile;
    }

    public String getLocalOboFile() {
        return localOboFile;
    }
}
