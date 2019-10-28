import cn.nyse.dao.GoodsDao;
import cn.nyse.entity.Goods;
import org.apache.lucene.analysis.Analyzer;

import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * @author zhanzs
 * @date 2019/10/28
 */
public class Demo_01 {

    GoodsDao goodsDao = new GoodsDao();

    @Test
    public void test1() {
        List<Goods> all = goodsDao.findAll();

        System.out.println(all);
    }

    /**
     * 建立索引
     *
     * @throws IOException
     */
    @Test
    public void createIndex() throws IOException {
        //1.采集原始数据
        List<Goods> goodsList = goodsDao.findAll();

        List<Document> docs = new ArrayList<Document>();
        for (Goods goods : goodsList) {
            //2.创建一篇文档
            Document doc = new Document();

            Integer id = goods.getId();
            String name = goods.getName();
            String title = goods.getTitle();
            Double price = goods.getPrice();
            String pic = goods.getPic();

            StringField id_filed = new StringField("id", id + "", Field.Store.YES);
            TextField name_filed = new TextField("name", name + "", Field.Store.YES);
            TextField title_filed = new TextField("title", title + "", Field.Store.YES);
            DoubleField price_filed = new DoubleField("price", price, Field.Store.YES);
            StoredField pic_filed = new StoredField("pic", pic);

            doc.add(id_filed);
            doc.add(name_filed);
            doc.add(title_filed);
            doc.add(price_filed);
            doc.add(pic_filed);

            docs.add(doc);

        }

        //3.创建分词器,用于分词
//        Analyzer analyzer = new StandardAnalyzer();

        //二元拆分
//        CJKAnalyzer cjkAnalyzer = new CJKAnalyzer();
        Analyzer analyzer = new IKAnalyzer();

        //4.操作索引库核心配置对象
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_3, analyzer);

        /**
         * 5.设置索引库打开的方式
         * CREATE: 每次运行都会把原来索引清空,建立新的索引
         * APPEND: 将本次的索引追加到索引库中
         * CREATE_OR_APPEND: 如果有索引就追加,如果没有就建立一个新的索引库
         */
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        //6.指定索引库位置
        FSDirectory dir = FSDirectory.open(new File("D://index"));

        //7.创建索引库操作对象,用于把文档写入索引库中
        IndexWriter writer = new IndexWriter(dir, config);

        for (Document doc : docs) {
            writer.addDocument(doc);

        }

        writer.commit();
        writer.close();

    }


    /**
     * 检索索引
     *
     * @throws Exception
     */
    @Test
    public void query() throws Exception {
        //关联索引库
        FSDirectory dir = FSDirectory.open(new File("D://index"));

        //索引库读取对象
        DirectoryReader reader = DirectoryReader.open(dir);

        //索引搜索对象
        IndexSearcher searcher = new IndexSearcher(reader);

        //创建解析器
        IKAnalyzer ikAnalyzer = new IKAnalyzer();

        //查看解析器
        QueryParser parser = new QueryParser(Version.LUCENE_4_10_3, "title", ikAnalyzer);

        //查询对象
        Query query = parser.parse("title:全网通4G手机");

        //数据(封装了doc,分值)
        TopDocs topDocs = searcher.search(query, 100);

        //数据(分值)
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;

        //查询到的数据数
        int totalHits = topDocs.totalHits;
        System.out.println("总共查到记录数:" + totalHits);

        for (ScoreDoc scoreDoc : scoreDocs) {
            //文档的id
            int docId = scoreDoc.doc;
            //匹配度
            float score = scoreDoc.score;

            Document doc = searcher.doc(docId);


            String id = doc.get("id");
            String name = doc.get("name");
            String title = doc.get("title");
            String price = doc.get("price");
            String pic = doc.get("pic");

            System.out.println("文档匹配度" + score);
            System.out.println("id:" + id);
            System.out.println("name" + name);
            System.out.println("title" + title);
            System.out.println("price" + price);
            System.out.println("pic" + pic);


        }
        reader.close();
    }


    /**
     * 删除索引库
     *
     * @throws IOException
     */
    @Test
    public void deleteIndex() throws IOException {

        //创建分词器
        Analyzer analyzer = new IKAnalyzer();

        //索引库配置对象
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_3, analyzer);

        //关联索引库
        FSDirectory dir = FSDirectory.open(new File("D://index"));
        IndexWriter writer = new IndexWriter(dir, config);

        //创建Term对象(条件对象)
        Term term = new Term("name", "小米");
        //根据Term删除文档
        writer.deleteDocuments(term);

        //删除全部
//        writer.deleteAll();

        writer.commit();
        writer.close();
    }


    /**
     * 修改索引
     *
     * @throws IOException
     */
    @Test
    public void updateIndex() throws IOException {
        //创建分词器
        IKAnalyzer ikAnalyzer = new IKAnalyzer();

        //创建索引库配置对象
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_3, ikAnalyzer);

        //创建索引库目录对象
        Directory dir = FSDirectory.open(new File("D://index"));

        IndexWriter writer = new IndexWriter(dir, config);

        //创建文档
        Document document = new Document();
        document.add(new StringField("id", "30", Field.Store.YES));
        document.add(new TextField("title", "修改后的手机", Field.Store.YES));
        document.add(new TextField("name", "荣耀", Field.Store.YES));
        document.add(new DoubleField("price", 2999, Field.Store.YES));

        //创建条件
        Term term = new Term("title", "华为");

        /**
         * 把通过term搜索到的文档都先删除,然后再添加一篇新文档,如果也没有搜索到那么则添加doc文档
         */
        writer.updateDocument(term, document);

        writer.commit();
        writer.close();

    }


    @Test
    public void booQuery() throws Exception {
        DirectoryReader reader = DirectoryReader.open(FSDirectory.open(new File("D://index")));

        IndexSearcher searcher = new IndexSearcher(reader);

        TermQuery termQuery=new TermQuery(new Term("title","神舟"));

        NumericRangeQuery<Double> query = NumericRangeQuery.newDoubleRange("price", 5000D, 10000D, true, false);


        //select * from goods where price >5000 and price<10000
        BooleanQuery booQuery = new BooleanQuery();

        /**
         * MUST:and 代表此条件必须要有
         * SHOULD: or 可，也可以没有
         * MUST_NOT:not  一定不能含有,排除
         */
        booQuery.add(termQuery, BooleanClause.Occur.MUST);
        booQuery.add(query, BooleanClause.Occur.MUST);


        System.out.println(booQuery);


        TopDocs topDocs = searcher.search(booQuery, 1000);

        print(topDocs.scoreDocs,searcher);

        reader.close();

    }

    public void print(ScoreDoc[] scoreDocs,IndexSearcher searcher) throws Exception{
        for (ScoreDoc scoreDoc : scoreDocs) {

            //文档的id
            int docId = scoreDoc.doc;
            float score = scoreDoc.score;


            Document doc = searcher.doc(docId);

            String id = doc.get("id");
            String name = doc.get("name");
            String title = doc.get("title");
            String price = doc.get("price");
            String pic = doc.get("pic");

            System.out.println("文档匹配度: " + score);
            System.out.println("id: " + id);
            System.out.println("name: " + name);
            System.out.println("title: " + title);
            System.out.println("price: " + price);
            System.out.println("pic: " + pic);

        }
    }
}
