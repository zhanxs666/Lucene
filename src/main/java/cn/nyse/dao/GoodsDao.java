package cn.nyse.dao;

import cn.nyse.entity.Goods;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhanzs
 * @date 2019/10/28
 */
public class GoodsDao {

    public List<Goods> findAll(){

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/lucene_db","root","root");

            Statement st= conn.createStatement();
            ResultSet rs = st.executeQuery("select * from goods");
            List<Goods> goodsList = new ArrayList<Goods>();

            while (rs.next()){
                Integer id = rs.getInt("id");
                String name = rs.getString("name");
                String title = rs.getString("title");
                Double price = rs.getDouble("price");
                String pic = rs.getString("pic");

                Goods goods = new Goods();
                goods.setId(id);
                goods.setName(name);
                goods.setTitle(title);
                goods.setPrice(price);
                goods.setPic(pic);

                goodsList.add(goods);
            }

            conn.close();

            return goodsList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
