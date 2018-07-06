package com.gmm;

import java.util.Random;

/**
 * K-Mean Algorithm  2014/9/25
 * @author Wang Wenfu
 *
 */
class KMeans {
	private int m_Ndim;
	private int m_Ncluster;
	private double[][] m_means;
	
	private int m_maxIter;
	private double m_endError;
	
	public KMeans(int Ndim, int Ncluster)
	{
		m_Ndim = Ndim;
		m_Ncluster = Ncluster;
		m_means = new double[m_Ncluster][m_Ndim]; //默认初始化为0
		m_maxIter = 1000;
		m_endError = 0.0001;
	}
	
	public void cluster(double[][] data, int[] labels, int size)
	{
		assert size > m_Ncluster;
		init(data, size);
		
		int label = -1;
		int iterNum = 0;
		double lastCost = 0.0; //均方差作为代价
		double currentCost = 0.0;
		int[] newLabels = new int[size];
		for (int i = 0; i < size; i++)
			newLabels[i] = -1;
		
		int unchanged = 0;
		Boolean loop = true;		
		double[] x = null;
		int[] counts = new int[m_Ncluster];
		double[][] next_means = new double[m_Ncluster][m_Ndim];
		
		while(loop){
			for(int i = 0; i < m_Ncluster; i++)
				counts[i] = 0;
			
			for(int i = 0; i < m_Ncluster; i++)
				for (int j = 0; j < m_Ndim; j++)
					next_means[i][j] = 0.0;
			
			lastCost = currentCost;
			currentCost = 0.0;
			
			for(int i = 0; i < size; i++){  //for 1
				x = data[i];
				
				double dist = -1;
				for(int k = 0; k < m_Ncluster; k++){
					double temp = calcuDistance(x, m_means[k], m_Ndim);
					if (temp < dist || dist == -1){
						dist = temp;
						label = k;
					}
				}
				currentCost += dist;
				
				labels[i] = label;
				counts[label]++;
				for(int d = 0; d < m_Ndim; d++){
					next_means[label][d] += x[d]; //此时存储数据的和
				}	
			}   //end of for 1
			currentCost /= size;
			for (int k = 0; k < m_Ncluster; k++){
				if(counts[k] > 0){
					for (int d = 0; d < m_Ndim; d++){
						next_means[k][d] /= counts[k];
						m_means[k][d] = next_means[k][d];  //不能直接数组赋值，传递的是引用，不安全
					}
				}				
			}
			//当本次聚类的样本标签与上次相同时，停止
			int index = 0;
			for(int i = 0; i < size; i++){
				if(newLabels[i] == labels[i])
					index++;
			}
			if(index == size) 
				loop = false;
			else{
				for(int i = 0; i < size; i++){
					newLabels[i] = labels[i];
				}
			}
			/*
			//终止条件
			iterNum++;
			if(Math.abs(lastCost - currentCost) < m_endError * lastCost)
				unchanged++;
			if(iterNum > m_maxIter || unchanged > 3)
				loop = false;  */
		}
		
	}
	
	
	//==================初始化各个簇的均值向量===================
	public void init(double[][] data, int size)
	{
		int[] randperm = new int[size];
		for(int i = 0; i < size; i++)
			randperm[i] = i;
		Random random = new Random();
		for(int i = 0; i < size; i++){
			int p = random.nextInt(size);
			int temp = randperm[i];
			randperm[i] = randperm[p];
			randperm[p] = temp;
		}
		/*
		for (int i = 0; i < size; i++ )
			System.out.print(randperm[i]+" ");
		System.out.println();
		*/
		for(int i = 0; i < m_Ncluster; i++)
			for(int d = 0; d < m_Ndim; d++)
				m_means[i][d] = data[ randperm[i] ][d];	 //随机选取K个点作为初值
	}
	
	public double getCostAndLabel(double[] x,int label)
	{
		double dist = -1;
		for(int k = 0; k < m_Ncluster; k++){
			double temp = calcuDistance(x, m_means[k], m_Ndim);
			if (temp < dist || dist == -1){
				dist = temp;
				label = k;
			}
		}
			
		return dist;
	}
	
	public double calcuDistance(double[] x, double[] y, int dim)
	{
		double temp = 0.0;
		for(int d = 0; d < dim; d++)
			temp += (x[d] - y[d]) * (x[d] - y[d]);
		return Math.sqrt(temp);
	}
	
	public double[] getMean(int i)
	{
		return m_means[i];
	}
}
