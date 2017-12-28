import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.ExtendedSSLSession;

public class HITS {
    //存储web图的数据结构
    private WebMenGraph graph;

    //包含每个网页的评分
    private Map<Integer,Double> hubScores;        //<id,value>

    //包含每个网页的authority
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

    //计算网页的Hub值和Authority值
    public void computeHITS(){
        computeHITS(25);
    }

    //计算网页的Hub和Authority评分
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

    //归一化数据集
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

    //返回与给定链接关联的Hub评分
    public Double hubScore(String link) {
        return hubScore(graph.URLToIdentifyer(link));
    }
    public Double hubScore(Integer id) {
        return (Double)(hubScores.get(id));
    }

    //初始化给定链接的Hub评分
    public void initializeHubScore(String link,double value) {
        Integer id = graph.URLToIdentifyer(link);
        if(id!=null)
            hubScores.put(id,new Double(value));
    }
    public void initializeHubScore(Integer id,double value) {
        if(id!=null)
            hubScores.put(id,new Double(value));
    }

    //返回与给定链接关联的Authority评分
    public Double authorityScore(String link) {
        return authorityScore(graph.URLToIdentifyer(link));
    }

    private Double authorityScore(Integer id) {
        return (double)(authorityScores.get(id));
    }

    //初始化与给定链接的Authoriy评分
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


//存储图的数据结构
public class WebGraphMemory {
    //把每个URL映射为一个整数，存储在web图中
    private Map<Integer,String> IdentifyToURL;

    //存储web图中URL之间关系的Map
    private Map<String,Map<String,Integer>> URLToIdentifyer;

    //存储入度，其中第一个参数是URL的ID，第二个参数是存放指向这个URL链接的Map，Double表示权重
    private Map<Integer,Map<Integer,Double>> Inlinks;

    //存储出度，其中第一个参数是URL的ID，第二个参数存放网页中的超链接，Double表示权重
    private Map<Integer,Map<Integer,Double>> OutLinks;

    //图中的节点数目
    private int nodeCount;

    //0个节点的构造函数
    public WebGraphMemory() {
        IdentifyToURL = new HashMap<Integer,String>();
        URLToIdentifyer = new HashMap<String,Map<String,Integer>>();
        InLinks = new HashMap<Integer,Map<Integer,Double>>();
        OutLinks = new HashMap<Integer,Map<Integer,Double>>();
        nodeCount = 0;
    }

    //从一个文本文件好脏取得接单的构造函数，每行包含一个指向关系
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

    //根据URL定制ID
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

    //根据ID获得URL
    public String IdentifyerToURL(Integer id) {
        return (IdentifyToURL.get(id));
    }

    //在图中增加一个节点
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

    //在两个节点中增加一个对应关系，如果节点不存在，新建节点
    public Double addLink(String fromLink,String toLink,Double weight) {
        Integer id1 = addLink(fromLink);
        Integer id2 = addLink(toLink);
        return addLink(id1,id2,weight);
    }

    //在两个节点中增加一个对应关系，如果节点不存在，则新建节点
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

    //针对指定的URL返回包含它入度的链接Map
    public Map inLinks(String URL) {
        Integer id = URLToIdentifyer(URL);
        return inLinks(id);
    }

    public Map<Integer,Double> inLinks(Integer link) {
        if(link == null) return null;
        Map<Integer,Double> aux = (InLinks.get(link));
        return aux;
    }

    //针对指定的URL返回包含它的出度的链接的Map
    public Map<Integer,Double> OutLinks(String URL) {
        Integer id = URLToIdentifyer(URL);
        return outLinks(id);
    }

    public Map<Integer,Double> outLinks(Integer link) {
        if(link == null) return null;
        Map<Integer,Double> aux = OutLinks.get(link);
        return aux;
    }

    //返回两个节点之间的权重，如果节点没有链接，返回0
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

    //把有向图变为无向图
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

    //删除内部链接，内部连接即指向同一主机的链接
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

    //删除内部导航链接
    public void removeNepotistic() {
        removeInternalLinks();
    }

    //删除 stop URL
    public void remoceStopLinks(String stopURLs[]) {
        HashMap aux = new HahsMap();
        for(int i=0;i<stopURLs.length;i++) 
           aux.put(stopURLs[i],null);
        removeStopLinks(aux);
    }

    //删除stop URL
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