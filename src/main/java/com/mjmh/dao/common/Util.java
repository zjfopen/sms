package com.mjmh.dao.common;

import sun.net.www.http.HttpClient;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.math.RoundingMode;
import java.net.*;
import java.text.NumberFormat;
import java.util.*;

public class Util
{
	private static final String TAG = "Util";

	public static String byte2HexString(byte abyte0[])
	{
		StringBuffer stringbuffer = new StringBuffer();
		int i = abyte0.length;
		int j = 0;
		do
		{
			if (j >= i) return stringbuffer.toString();
			stringbuffer.append(Integer.toHexString(0x100 | 0xff & abyte0[j]).substring(1));
			j++;
		}
		while (true);
	}

	public static long getUTCTime()
	{
		Calendar cal = Calendar.getInstance(Locale.CHINA);
		int zoneOffset = cal.get(Calendar.ZONE_OFFSET);
		int dstOffset = cal.get(Calendar.DST_OFFSET);
		cal.add(Calendar.MILLISECOND, -(zoneOffset + dstOffset));
		return cal.getTimeInMillis();
	}

	public static boolean intToBool(int source)
	{
		if (source == 1)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public static int boolToInt(boolean source)
	{
		if (source)
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}

	public static Object deepClone(Object src)
	{
		Object dst = null;

		try
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream oo = new ObjectOutputStream(out);
			oo.writeObject(src);

			ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
			ObjectInputStream oi = new ObjectInputStream(in);
			dst = oi.readObject();
		}
		catch (Exception e)
		{
			//Log.e(TAG, e.getMessage(), e.getCause());
		}

		return dst;
	}

	

	
	 public static String geocodeAddr(String latitude, String longitude) { 
           String addr = ""; 
           String url = String.format("http://ditu.google.cn/maps/geo?output=csv&key=abcdef&q=%s,%s",latitude, longitude); 
           URL myURL = null; 
           URLConnection httpsConn = null; 
           try { 
               myURL = new URL(url); 
           } catch (MalformedURLException e) { 
               e.printStackTrace(); 
               return null; 
           } 
           
           try { 
               httpsConn = (URLConnection) myURL.openConnection(); 
               if (httpsConn != null) {
                   InputStreamReader insr = new InputStreamReader(httpsConn 
                   .getInputStream(), "UTF-8"); 
                   BufferedReader br = new BufferedReader(insr); 
                   String data = null; 
                   if ((data = br.readLine()) != null) { 
                       System.out.println(data); 
                       String[] retList = data.split(","); 
                       if (retList.length > 2 && ("200".equals(retList[0]))) { 
                           addr = retList[2]; 
                           addr = addr.replace("\"", ""); 
                       } else { 
                           addr = ""; 
                       } 
                   } 
                   insr.close(); 
               } 
           } catch (IOException e) { 
               e.printStackTrace(); 
               return null; 
           } 
           return addr; 
       }
	 /**
		 * 检测字符串是否不为空(null,"","null")
		 * 
		 * @param s
		 * @return 不为空则返回true，否则返回false
		 */
		public static boolean notEmpty(String s) {
			return s != null && !"".equals(s) && !"null".equals(s);
		}

		/**
		 * 检测字符串是否为空(null,"","null")
		 * 
		 * @param s
		 * @return 为空则返回true，不否则返回false
		 */
		public static boolean isEmpty(String s) {
			return s == null || "".equals(s) || "null".equals(s);
		}
		
		/**
		 * 去除 list重复数据
		 * @param list
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public   static   List  removeDuplicate(List list)   { 
			if(list!=null){
				HashSet h  =   new  HashSet(list); 
			    list.clear(); 
			    list.addAll(h); 
			    return list;
			}
			return null;
			
		} 
		/**
		 * 对象转String
		 * @param obj
		 * @return
		 */
		public static String objectToString(Object obj){
			if(obj == null){
				return "";
			}else{
				return obj.toString();
			}
		}
		/**
		 * 获取外网IP
		 * @param request
		 * @return
		 */
		public static String getIpAddr(HttpServletRequest request) { 
			String ipAddress = null;
			// ipAddress = this.getRequest().getRemoteAddr();
			ipAddress = request.getHeader("x-forwarded-for");
			if (ipAddress == null || ipAddress.length() == 0
					|| "unknown".equalsIgnoreCase(ipAddress)) {
				ipAddress = request.getHeader("Proxy-Client-IP");
			}
			if (ipAddress == null || ipAddress.length() == 0
					|| "unknown".equalsIgnoreCase(ipAddress)) {
				ipAddress = request.getHeader("WL-Proxy-Client-IP");
			}
			if (ipAddress == null || ipAddress.length() == 0
					|| "unknown".equalsIgnoreCase(ipAddress)) {
				ipAddress = request.getRemoteAddr();
				if (ipAddress.equals("127.0.0.1")) {
					// 根据网卡取本机配置的IP
					InetAddress inet = null;
					try {
						inet = InetAddress.getLocalHost();
					} catch (UnknownHostException e) {
						e.printStackTrace();
					}
					ipAddress = inet.getHostAddress();
				}

			}

			// 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
			if (ipAddress != null && ipAddress.length() > 15) { // "***.***.***.***".length()
																// = 15
				if (ipAddress.indexOf(",") > 0) {
					ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
				}
			}
			return ipAddress;
			}


		private static String html2char(String str1) {
			//String str1 = "&#29305;&#21035;";
			String[] str = str1.split(";");

			String sResult = "";
			for (int i = 0; i < str.length; i++) {
				int str2 = Integer.parseInt(str[i].replace("&#", ""));
				sResult += "" + (char) str2;
			}
			return sResult;
		}
		/**
		 * 获取Exception详情
		 * @param ex
		 * @return
		 */
		public static String getExceptionAllinformation(Exception ex){
			 ByteArrayOutputStream out = new ByteArrayOutputStream();
	         PrintStream pout = new PrintStream(out);
	         ex.printStackTrace(pout);
	         String ret = new String(out.toByteArray());
	         try {
	         pout.close();
	         } catch (Exception e) {
	         }
	         try {
	              out.close();
	         } catch (Exception e) {
	         }
	         return ret;
	 }
		
	public static String getJDtf(String b){
		Integer bl = Integer.valueOf(b);
		String msg = "";
		if(bl == 0){
			msg = "否";
		}else if(bl == 1){
			msg = "是";
		}
		return msg;
		
	}
	public static List<String> readTxtFile(String fileName) {
	        // TODO Auto-generated method stub
	    File file = new File(fileName);
	    BufferedReader reader = null;
	    InputStreamReader isr=null;
	    String tempString = null;
	    List<String> list=new ArrayList<String>();
	    try {
	    	isr = new InputStreamReader(new FileInputStream(file), "GB2312");
	    	reader = new BufferedReader(isr);
	        while ((tempString = reader.readLine()) != null) {
	            list.add(tempString);
	        }
	        return list;
	    } catch (FileNotFoundException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }finally{
	        if(reader != null){
	            try {
	                reader.close();
	            } catch (IOException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	            }
	        }
	        if(isr != null){
	            try {
	            	isr.close();
	            } catch (IOException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	            }
	        }
	    }
	    return list;
	}	
	
	/**
	 * 数据为null  -->  0
	 * @param object
	 * @return
	 */
	public static Double isNullToNumber(Object object){
		if(object == null){
			return 0d;
		}
		return  (Double) object;
	}
	
	// 计算两个数的百分比
	public static String getPercent(Double num1, Double num2) {
		Double result = (double) 0;
		if (num2 != null && num2 != 0) {
			result = num1 / num2;
		}
		NumberFormat fromat1 = NumberFormat.getPercentInstance();
		fromat1.setMinimumFractionDigits(2);// 设置保留小数位
		fromat1.setRoundingMode(RoundingMode.HALF_UP); // 设置舍入模式
		return fromat1.format(result);
	}
	
	// poi根据字符数计算单元格宽度
	public static int getCellWidthByCount(int size) {
		return "宽".getBytes().length * 256 * size;
	}
}
