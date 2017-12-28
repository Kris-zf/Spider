import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.ExtendedSSLSession;

public class HITS {
    //�洢webͼ�����ݽṹ
    private WebMenGraph graph;

    //����ÿ����ҳ������
    private Map<Integer,Double> hubScores;        //<id,value>

    //����ÿ����ҳ��authority
    private Map<Integer,Double> authorityScores;  //<id,value>

    public HITS (WebMenGraph graph) {
        this.graph = graph;
        this.hubScores = new HashMap<Integer,Double>();
        this.authorityScores = new HashMap<Integer,Double>();
        int numLinks = graph.numNodes();
        for(int i=1;i<numLinks;i++){
            hubScores.put(new Integer(i),new Double(1));
            authorityScores.put(new Integer(i),new Double(1));
        }
        computeHITS();
    }

    //������ҳ��Hubֵ��Authorityֵ
    public void computeHITS(){
        computeHITS(25);
    }

    //������ҳ��Hub��Authority����
    public void computeHITS(int numIterations) {
        while (numIterations-->0) {
            for(int i=1;i<=graph.numNodes();i++){
                Map<Integer,Double> inlinks = graph.inLinks(new Integer(i));
                Map<Integer,Double> outlinkd = graph.outLinks(new Integer(i));
                double authorityScore = 0;
                double hubScore = 0;
                for(Integer id:inlinks.keySet()) {
                    authorityScore += (hubScores.get(id)).doubleValue();
                }

                for(Integer id:outlinks.keySet()) {
                    hubScore += (authorityScores.get(id)).doubleValue();
                }

                authorityScores.put(new Integer(i),new Double(authorityScores));
                hubScores.put(new Integer(i),new Double(hubScores));
            }
            normalize(authorityScores);
            normalize(hubScores);            
        }
    }

    public void computeWeighedtHITS(int numIterations) {
        while(numIterations-->0){
            for(int i=1;i<=graph.numNodes();i++){
                Map<Integer,Double> inlinks = graph.inLinks(new Integer(i));
                Map<Integer,Double> outlinks = graph.outLinks(new Integer(i));
                double authorityScore = 0;
                double hubScore = 0;
                for(Entry<Integer,Double> in:inlinks.entrySet()) {
                    authorityScore += (hubScores.get(in.getKey())).doubleValue() * in.getValue();
                }

                for(Entry<Integer,Double> out:outlinks.entrySet()) {
                    hubScore += (authorityScores.get(out.getKey())).doubleValue() * out.getValue();
                }

                authorityScores.put(new Integer(i),new Double(authorityScore));
                hubScores.put(new Integer(i),new Double(hubScore));
            }
            normalize(authorityScores);
            normalize(hubScores);
        }
    }

    //��һ�����ݼ�
    private void normalize(Map<Integer,Double> scoreSet){
        Iterator<Integer> iter = scoreSet.KeySet().iterator();
        double summation = 0.0;
        while(iter.hasNext())
           summation += ((scoreSet.get((Integer)(iter.next())))).doubleValue();
        
        iter  = scoreSet.keySet().iterator();
        while(iter.hasNext()){
            Integer id = iter.next();
            scoreSet.put(id, (scoreSet.get(id)).doubleValue() / summation);
        }
    }

    //������������ӹ�����Hub����
    public Double hubScore(String link) {
        return hubScore(graph.URLToIdentifyer(link));
    }
    public Double hubScore(Integer id) {
        return (Double)(hubScores.get(id));
    }

    //��ʼ���������ӵ�Hub����
    public void initializeHubScore(String link,double value) {
        Integer id = graph.URLToIdentifyer(link);
        if(id!=null)
            hubScores.put(id,new Double(value));
    }
    public void initializeHubScore(Integer id,double value) {
        if(id!=null)
            hubScores.put(id,new Double(value));
    }

    //������������ӹ�����Authority����
    public Double authorityScore(String link) {
        return authorityScore(graph.URLToIdentifyer(link));
    }

    private Double authorityScore(Integer id) {
        return (double)(authorityScores.get(id));
    }

    //��ʼ����������ӵ�Authoriy����
    public void initializeAuthorityScore(String link,double value) {
        Integer id = graph.URLToIdentifyer(link);
        if(id!=null)
            authorityScores.put(id,new Double(value));
    }
    public void initializeAuthorityScore(Integer id,double value) {
        if(id!=null)
            authorityScores.put(id,new Double(value));
    }
}


//�洢ͼ�����ݽṹ
public class WebGraphMemory {
    //��ÿ��URLӳ��Ϊһ���������洢��webͼ��
    private Map<Integer,String> IdentifyToURL;

    //�洢webͼ��URL֮���ϵ��Map
    private Map<String,Map<String,Integer>> URLToIdentifyer;

    //�洢��ȣ����е�һ��������URL��ID���ڶ��������Ǵ��ָ�����URL���ӵ�Map��Double��ʾȨ��
    private Map<Integer,Map<Integer,Double>> Inlinks;

    //�洢���ȣ����е�һ��������URL��ID���ڶ������������ҳ�еĳ����ӣ�Double��ʾȨ��
    private Map<Integer,Map<Integer,Double>> OutLinks;

    //ͼ�еĽڵ���Ŀ
    private int nodeCount;

    //0���ڵ�Ĺ��캯��
    public WebGraphMemory() {
        IdentifyToURL = new HashMap<Integer,String>();
        URLToIdentifyer = new HashMap<String,Map<String,Integer>>();
        InLinks = new HashMap<Integer,Map<Integer,Double>>();
        OutLinks = new HashMap<Integer,Map<Integer,Double>>();
        nodeCount = 0;
    }

    //��һ���ı��ļ�����ȡ�ýӵ��Ĺ��캯����ÿ�а���һ��ָ���ϵ
    public WebGraphMemory(File file) throws IOException,FileNotFoundException {
        this();
        BufferedReader reader = new BUfferedReader(new FileReader(file));
        String line;
        while((line=reader.readLine())!=null){
            int index1 = line.indexOf("->");
            if(index1==-1)
                addLink(line.trim());
            else{
                String url1 = line.substring(0,index1).trim();
                String url2 = line.substring(index+2).trim();
                Double strength = new Double(1.0);
                index1 = url2.indexOf(" ");
                if(index1!=-1)
                   try{
                       strength = new Double(url2.substring(index1+1).trim());
                       url2 = url2.substring(0,index1).trim();
                   }catch(Exception e) {}
                   addLink(url1,url2,strength);
            }
        }
    }

    //����URL����ID
    public Integer uRLToIdentityer(String URL) {
        String host,name;
        int index = 0,index2 = 0;
        if(URL.startsWith("http://"))   index = 7;
        else if(URL.startsWith("ftp://")) index = 6;
        index2 = URL.substrnig(index).indexOf("/");
        if(index2!=-1){
            name = URL.substring(index+index2+1);
            host = URL.substring(0,index+index2);
        }else{
            host = URL;
            name = "";
        }
        Map<String,Integer> map = (URLToIdentifyer.get(host));
        if(map == null)
           return null;
        return (map.get(name));
    }

    //����ID���URL
    public String IdentifyerToURL(Integer id) {
        return (IdentifyToURL.get(id));
    }

    //��ͼ������һ���ڵ�
    public Integer addLink(String link) {
        Integer id = URLToIdentifyer(link);
        if(id==null) {
            id =new Integer(++nodeCount);
            String host;
            String name;
            int index=0,index2=0;
            if(link.startsWith("http://")) index=7;
            else if(link.startsWith("ftp://")) index=6;
            index2 = link.substring(index).indexOf("/");
            if(index2!=-1){
                name = link.substring(index+index2+1);
                host = link.substring(0,index+index2);
            }else{
                host = link;
                name = "";
            }
            System.out.println("HOST:"+host+"name:"+name);
            Map<String,Integer> map = (URLToIdentifyer.get(host));
            if(map==null){
                map = new HashMap<String,Integer>();
                URLToIndentfyer.put(host,map);
            }
            map.put(name,id);
            IdentifyToURL.put(id,link);
            InLinks.put(id,new HashMap<Integer,Double>());
            OutLinks.put(id,new HashMap<Integer,Double>());
        }
        return id;
    }

    //�������ڵ�������һ����Ӧ��ϵ������ڵ㲻���ڣ��½��ڵ�
    public Double addLink(String fromLink,String toLink,Double weight) {
        Integer id1 = addLink(fromLink);
        Integer id2 = addLink(toLink);
        return addLink(id1,id2,weight);
    }

    //�������ڵ�������һ����Ӧ��ϵ������ڵ㲻���ڣ����½��ڵ�
    private Double addLink(Integer fromLink,Integer toLink,Double weight) {
        Double aux;
        Map<Integer,Double> map1 = (InLinks.get(toLink));
        Map<Integer,Double> map2 = (OutLinks.get(fromLink));
        aux = (Double)(map1.get(fromLink));
        if(aux==null) map1.put(fromLink,weight);
        else if(aux.doubleValue()<weight.doubleValue())
             map1.put(fromLink,weight);
        else
             weight = new Double(aux.doubleValue());

        aux = (map2.get(toLink));
        if(aux==null) map2.put(toLink,weight);
        else if(aux.doubleValue()<weight.doubleValue())
             map2.put(toLink,weight);
        else{
            weight = new Double(aux.doubleValue());
            map1.put(fromLink,weight);
        }
        InLinks.put(toLink,map1);
        OutLinks.put(fromLink,map2);

        return weight;
    }

    //���ָ����URL���ذ�������ȵ�����Map
    public Map inLinks(String URL) {
        Integer id = URLToIdentifyer(URL);
        return inLinks(id);
    }

    public Map<Integer,Double> inLinks(Integer link) {
        if(link == null) return null;
        Map<Integer,Double> aux = (InLinks.get(link));
        return aux;
    }

    //���ָ����URL���ذ������ĳ��ȵ����ӵ�Map
    public Map<Integer,Double> OutLinks(String URL) {
        Integer id = URLToIdentifyer(URL);
        return outLinks(id);
    }

    public Map<Integer,Double> outLinks(Integer link) {
        if(link == null) return null;
        Map<Integer,Double> aux = OutLinks.get(link);
        return aux;
    }

    //���������ڵ�֮���Ȩ�أ�����ڵ�û�����ӣ�����0
    public Double inLink(String fromLink,String toLink) {
        Integer id1 = URLToIdentifyer(fromLink);
        Integer id2 = URLToIdentifyer(toLink);
        return inLink(id1,id2);
    }

    public Double outLink(String fromLink,String toLink) {
        Integer id1 = URLToIdentifyer(fromLink);
        Integer id2 = URLToIdentifyer(toLink);
        return outLink(id1,id2);
    }

    public Double inLink(Integer fromLink,Integer toLink) {
        Map<Integer,Double> aux = inLinks(toLink);
        if(aux==null) return new Double(0);
        Double weight = (aux.get(fromLink));
        return (weight == null) ? new Double(0) : weight;
    }

    public Double outLink(Integer fromLink,Integer toLink) {
        Map<Integer,Double> aux = outLinks(fromLinks);
        if(aux==null) return new Double(0);
        Double weight = (aux.get(toLink));
        return (weight == null) ? new Double(0) : weight;
    }

    //������ͼ��Ϊ����ͼ
    public void transformUnidirectional() {
        Iterator it = OutLinks.keySet().iterator();
        while(it.hasNext()){
            Integer link1 = (Integer)(it.next());
            Map<K,V> auxMap = (Map)(OutLinks.get(link1));
            Iterator it2 = auxMap.keySet().iterator();
            while(it2.hasNext()){
                Integer link2 = (Integer)(it.next());
                Double weight = (Double)(auxMap.get(link2));
                addLink(link2,link1,weight);
            }
        }
    }

    //ɾ���ڲ����ӣ��ڲ����Ӽ�ָ��ͬһ����������
    public void removeInternalLinks() {
        int index1;
        Iterator it =OutLinks.keySet().iterator();
        while(it.hasNext()) {
            Integer link1 = (Integer)(it.next());
            Map<Integer,Double> auxMap = (OutLinks.get(link1));
            Iterator it2 = auxMap.keySet().iterator();
            if(it2.hasNext()) {
                String URL1=(String)(IdentifyerToURL.get(link1));
                index1 = URL1.indexOf("://");
                if(index1!=-1) URL1=URL1.substring(index1+3);
                index1 = URL1.indexOf("/");
                if(index1!=-1) URL1=URL1.substring(0,index1);
                while(it2.hasNext()) {
                    Integer link2 = (Integer)(it.next());
                    String URl2 = (String)(IdentifyerToURL.get(link2));
                    index1 = URL2.indexOf("://");
                    if(index1!=-1) URL2=URL1.substring(index1+3);
                    index1 = URL2.indexOf("/");
                    if(index1!=-1) URL2=URL1.substring(0,index1);
                    if(URL1.equals(URL2)) {
                        auxMap.remove(link2);
                        OutLinks.put(link1,auxMap);
                        auxMap = (InLinks.get(link2));
                        auxMap.remove(linkl1);
                        InLinks.put(link2,auxMap);
                    }
                }
            }
        }
    }

    //ɾ���ڲ���������
    public void removeNepotistic() {
        removeInternalLinks();
    }

    //ɾ�� stop URL
    public void remoceStopLinks(String stopURLs[]) {
        HashMap aux = new HahsMap();
        for(int i=0;i<stopURLs.length;i++) 
           aux.put(stopURLs[i],null);
        removeStopLinks(aux);
    }

    //ɾ��stop URL
    public void removeStopLinks(Map stopURLs) {
        int index1;
        Iterator it = OutLinks.keySet().iterator();
        while(it.hasNext()){
            Integer link1 = (Integer)(it.next());
            String URL1 = (String)(IdentifyerToURL.get(link1));
            index1 = URL1.indexOf("://");
            if(index1!=-1) URL1=URL1.substring(index1+3);
            index1 = URl1.indexOf("/");
            if(index1!=-1) URL1=URL1.substring(0,index1);
            if(stopURLs.containKey(URL1)) {
                OutLinks.put(link1,new HashMap());
                Inlinks.put(link1,new HashMap());
            }
        }
    }

    public int numNodes(){
        return nodeCount;
    }

}