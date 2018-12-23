package dao;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import javax.servlet.ServletContext;


public class CommDAO
{
	
	Connection conn = null;
	
	public CommDAO()
	{
		conn = this.getConn();
	}
	
	
	
	private static Properties config = null;

	 static {
		 try {
			
	  config = new Properties(); 
	  // InputStream in = config.getClass().getResourceAsStream("dbconnection.properties");
	
     InputStream in =  CommDAO.class.getClassLoader().getResourceAsStream("dbconnection.properties");

	  
	 
	   config.load(in);
	  
	   in.close();
	  } catch (Exception e) {
	  e.printStackTrace();
	  }
	 }
	
	
	
	public Connection getConn()
	{
	        try
	        {
	        	String d = new Date().toLocaleString().substring(0,9);
	            Class.forName("com.mysql.jdbc.Driver");
	            String dburl = (String)config.get("dburl");
	            conn = DriverManager.getConnection(dburl,"root","123");
	        	
//	            String d = new Date().toLocaleString().substring(0,9);
//	        	Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
//	            String dburl = (String)config.get("dburl");
//	            conn = DriverManager.getConnection(dburl);
	        }
	        catch(Exception e)
	        {
	            e.printStackTrace();
	        }
	        return conn;
	}
	
	
	public int getInt(String sql)
	{
		int i = 0;
		try {
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(sql);
			if(rs.next())
			{
				i = rs.getInt(1);
			}
			    st.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return i;
	}
	
	
	public double getDouble(String sql)
	{
		double i = 0;
		try {
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(sql);
			if(rs.next())
			{
				i = rs.getDouble(1);
				
			}
			    st.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return i;
	}
	
	public void commOper(String sql)
	{
		try {
			Statement st = conn.createStatement();
		    st.execute(sql);
		    st.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void commOperSqls(ArrayList<String> sql)
	{
		
		try {
			conn.setAutoCommit(false);
			for(int i=0;i<sql.size();i++)
			{
			Statement st = conn.createStatement();
			
		    st.execute(sql.get(i));
		    st.close();
			}
			conn.commit();
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}finally{
			try {
				conn.setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public List<HashMap> select(String sql)
	{
		

		List<HashMap> list = new ArrayList();
		try {
			Statement st = conn.createStatement();
		    ResultSet rs = st.executeQuery(sql);
		    ResultSetMetaData rsmd = rs.getMetaData();
           
		    while(rs.next())
		    {
		    	HashMap map = new HashMap();
		    	int i = rsmd.getColumnCount();
		    	for(int j=1;j<=i;j++)
		    	{
		    		if(!rsmd.getColumnName(j).equals("ID"))
		    		{
		    		
    	    	map.put(rsmd.getColumnName(j), rs.getString(j)==null?"":rs.getString(j));
		    		}
		    		else
		    		{
		    		
		    			map.put("id", rs.getString(j));
		    		}
		    	}
		    	list.add(map);
		    }
		    rs.close();
		    st.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("--"+sql);
			e.printStackTrace();
		}
		return list;
	}
	
	
	public void close()
	{
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 执行一条查询sql,以 List<hashmap> 的形式返回查询的记录，记录条数，和从第几条开始，由参数决定，主要用于翻页
	 * pageno 页码  rowsize 每页的条数
	 */
	public List select(String sql, int pageno, int rowsize) {
		List<HashMap> list=new ArrayList<HashMap>();
		List<HashMap> mlist=new ArrayList<HashMap>();
		try{
			list=this.select(sql);
			int min = (pageno-1)*rowsize;
			int max = pageno*rowsize;
			
			for(int i=0;i<list.size();i++)
			{
				
				if(!(i<min||i>(max-1)))
				{
				mlist.add(list.get(i));
				}
			}
		}catch(RuntimeException re){
			re.printStackTrace();
			throw re;
		}
		
		
		return mlist;
	}
	
	
	 //	该方法返回一个table 用于流动图片
	public String DynamicImage(String categoryid,int cut,int width,int height){

		StringBuffer imgStr = new StringBuffer();
		StringBuffer thePics1 = new StringBuffer();
		StringBuffer theLinks1 = new StringBuffer();
		StringBuffer theTexts1 = new StringBuffer();
	
		imgStr.append("<div id=picViwer1 align=center></div><SCRIPT src='/databasesys/js/dynamicImage.js' type=text/javascript></SCRIPT>\n<script language=JavaScript>\n");
		thePics1.append("var thePics1=\n'");
		theLinks1.append("var theLinks1='");
		theTexts1.append("var theTexts1='");
		
		List<HashMap> co = this.select("select * from  news where title!='系统简介计算机课程管理系统' and  title!='毕业设计栏目管理' order by id desc", 1, 6);
		int j = 0;
		int i = co.size();
		for(HashMap b:co)
		{
			j++;
		String id = b.get("id").toString();
		String title = b.get("title").toString();
		
		String url = "/databasesys/upfile/"+b.get("picurl");
		
		String purl = "";
		
		if(j!=i){
		thePics1.append(url.replaceAll("\n", "")+"|");
		theLinks1.append(purl+"|");
		theTexts1.append(title+"|");
		}
		if(j==i)
		{
			thePics1.append(url.replaceAll("\n", ""));
			theLinks1.append("#");
			theTexts1.append(title);
		}
		
		}
	   thePics1.append("';");
		
		theLinks1.append("';");
		theTexts1.append("';");
		imgStr.append(thePics1+"\n");
		imgStr.append(theLinks1+"\n");
		imgStr.append(theTexts1+"\n");
		imgStr.append("\n setPic(thePics1,theLinks1,theTexts1,"+width+","+height+",'picViwer1');</script>");
		return imgStr.toString();
	}
	

	
	
	public static void main(String[] args) {
		System.out.println(new CommDAO());
	}
}
