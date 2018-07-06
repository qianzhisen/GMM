package com.gmm;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONObject;

public class DocmentSetjsonlist {
//dl, ds, w, XL, XS, XLieheL, ZLable
	int Dl,Ds;//�ĵ�����
	int[][] XL,XS;//�ĵ�-��������
	int[] ZclusterS,ZclusterL,XLieheL,XLieheS,XLiehe;//���ĵ�������������������Ͽ��е����д�Ƶ����
	ArrayList<String> wordId= new ArrayList<String>();//Ϊ�����ɱ��ÿ���ʶ�Ӧ���±��ʹ�õ�list����
	
	BufferedReader readerL,readerLL,readerS,readerSS;//��Ҫ�����json�ı���ʽ�����Ͽ��ļ�
	
	public DocmentSetjsonlist(String strL,String strS) throws IOException{
		this.Dl=0;
		this.Ds=0;
		initialize(strL,strS);
		statisticX(strL,strS);
	}
	//��ʼ�����Ͽ��е����д��б�list����-���ִ��� ��ֵ��map���Լ��ĵ�����Ds
	public void initialize(String resetPointerL,String resetPointerS) throws IOException{
		String str;
		this.readerL=new BufferedReader(new FileReader(resetPointerL));
		this.readerS=new BufferedReader(new FileReader(resetPointerS));
		/*readerL.mark((int)new File(resetPointerL).length()+1);
		readerS.mark((int)new File(resetPointerS).length()+1);*/
		//ͳ�Ƴ��ı�
		while((str=readerL.readLine())!=null){
			JSONObject jsonObject = new JSONObject(str);
			String[] split = jsonObject.getString("text").split("\\s");
			//System.out.println(Arrays.toString(split));
			for(int i=0;i<split.length;i++)
				if(wordId.indexOf(split[i])==-1)
					wordId.add(split[i]);
			Dl++;
		}
		//ͳ�ƶ��ı�
		while((str=readerS.readLine())!=null){
			JSONObject jsonObject = new JSONObject(str);
			String[] split = jsonObject.getString("text").split("\\s");
			//System.out.println(Arrays.toString(split));
			for(int i=0;i<split.length;i++)
				if(wordId.indexOf(split[i])==-1)
					wordId.add(split[i]);
			Ds++;
		}
		XL=new int[Dl][wordId.size()];
		XS=new int[Ds][wordId.size()];
		ZclusterS=new int[Ds];//���ı�����������
		ZclusterL=new int[Dl];
		XLieheL=new int[wordId.size()];//���ı����кͣ���Ϊ�����ǵ�ģ����ֻ�õ��˳��ı����к�����
		XLieheS=new int[wordId.size()];
		XLiehe=new int[wordId.size()];
		/*readerL.reset();
		readerS.reset();*/
		readerL.close();
		readerS.close();
	}
	//ͳ�Ƹ�ֵX��Zcluster����
	public void statisticX(String resetPointerL,String resetPointerS) throws IOException{
		String str;
		int dl=0,ds=0;
		this.readerLL=new BufferedReader(new FileReader(resetPointerL));
		this.readerSS=new BufferedReader(new FileReader(resetPointerS));
		//�����ı�
		while((str=readerLL.readLine())!=null){
			JSONObject jsonObject = new JSONObject(str);
			String[] split = jsonObject.getString("text").split("\\s");
			for(int i=0;i<split.length;i++){
				XL[dl][wordId.indexOf(split[i])]++;
				XLieheL[wordId.indexOf(split[i])]++;
				XLiehe[wordId.indexOf(split[i])]++;
			}
			ZclusterL[dl]=jsonObject.getInt("cluster");
			dl++;
		}
		//������ı�
		while((str=readerSS.readLine())!=null){
			JSONObject jsonObject = new JSONObject(str);
			String[] split = jsonObject.getString("text").split("\\s");
			for(int i=0;i<split.length;i++){
				XS[ds][wordId.indexOf(split[i])]++;
				XLieheS[wordId.indexOf(split[i])]++;
				XLiehe[wordId.indexOf(split[i])]++;
			}
			ZclusterS[ds]=jsonObject.getInt("cluster");
			ds++;
		}
		readerLL.close();
		readerSS.close();
	}
	
	
	
	public int getDl() {
		return Dl;
	}
	
	public int getDs() {
		return Ds;
	}
	
	public int[][] getXL() {
		return XL;
	}
	
	public int[][] getXS() {
		return XS;
	}
	
	public int[] getZclusterS() {
		return ZclusterS;
	}
	
	public int[] getZclusterL() {
		return ZclusterL;
	}
	
	public int[] getXLieheL() {
		return XLieheL;
	}
	
	public int[] getXLiehe() {
		return XLiehe;
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		
		DocmentSetjsonlist doc = new DocmentSetjsonlist("long034.txt","twitter_short034.txt");
		
		int dl=doc.getDl();
		int ds=doc.getDs();
		int w=doc.getXLieheL().length;
		int[][] XL=doc.getXL();
		int[][] XS=doc.getXS();//�ʵĶ�����Ҫ����һ�£�w��,��Ϊ�����ı��������Ͽ��е�ȫ���������ɵ�
		int[] ZLableS=doc.getZclusterS();
		int[] ZLableL=doc.getZclusterL();
		int[] XLieheL=doc.getXLieheL();
		/*
		System.out.println(dl+" "+ds+" "+w+" "+XL.length+" "+XS.length+" "+ZLable.length+" "+XLieheL.length+" "+XS[0].length+" "+XL[0].length);
		int j=0,l=0;
		for(int i=0;i<XS[0].length;i++){
			j+=XS[3][i];
		}
		for(int i=0;i<XL[0].length;i++){
			l+=XL[599][i];
		}
		System.out.println(j+" "+l);
		*/
		
		/*BufferedWriter writer = new BufferedWriter(new FileWriter("data\\xlieheS22.txt"));
		BufferedWriter writer1 = new BufferedWriter(new FileWriter("data\\XS22.txt"));
		for(int i=0;i<XLieheL.length;i++){
			writer1.write(String.format(XS[0][i]+" ","gbk"));
			writer1.flush();
		}
		writer1.close();
		for(int i=0;i<20;i++)
			System.out.print(XL[0][i]);
		int m=0;
		for(int i=0;i<XL[0].length;i++){
			m+=XL[0][i];
		}
		System.out.print(m);
		*/
		BufferedWriter writer = new BufferedWriter(new FileWriter("12.txt"));
		for(int i=0;i<XS.length;i++){
			writer.write(Arrays.toString(XS[i]).replaceAll("\\D", " ").trim()/*+" "+ZLableL[i]*/);
			writer.newLine();
			writer.flush();
		}
		writer.close();
		
	}

}
