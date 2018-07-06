package com.gmm;
/**
 *  GMM Algorithm     2014/9/25 
 * @author Wang Wenfu
 *
 */
class GMM {
	private int m_Ndim;
	private int m_Ncompo;
	private double[] m_priors;
	private double[][] m_means; //每一行存放均值向量
	private double[][] m_vars;  //每一行存放对角协方差
	
	private double[] m_minVars;
	private int m_maxIter;
	private double m_endError;
	
	public GMM(int Ndim, int Ncompo)
	{
		m_Ndim = Ndim;
		m_Ncompo = Ncompo;
		m_maxIter = 1000;
		m_endError = 0.0001;
		
		m_priors = new double[m_Ncompo];
		m_means = new double[m_Ncompo][m_Ndim];
		m_vars = new double[m_Ncompo][m_Ndim];
		m_minVars = new double[m_Ndim];
		
		for(int k = 0; k < m_Ncompo; k++)
		{
			m_priors[k] = 1.0 / m_Ncompo;
			for(int d = 0; d < m_Ndim; d++)
			{
				m_means[k][d] = 0.0;
				m_vars[k][d] = 1.0;
			}
		}
	}
	
	public void train(double[][] data, int size)
	{
		init(data, size);
		Boolean loop = true;
		int iterNum = 0;
		double lastL = 0.0;
		double currL = 0.0;
		int unchanged = 0;
		double[] x = null;
		double[] next_priors = new double[m_Ncompo];
		double[][] next_means = new double[m_Ncompo][m_Ndim];
		double[][] next_vars = new double[m_Ncompo][m_Ndim];
		
		while(loop)
		{
			//先验、均值、协方差置0
			for(int k = 0; k < m_Ncompo; k++)
				next_priors[k] = 0.0;
			for(int k = 0; k < m_Ncompo; k++)
			{
				for(int d = 0; d < m_Ndim; d++){
					next_means[k][d] = 0.0;
					next_vars[k][d] = 0.0;
				}
			}
			
			lastL = currL;
			currL = 0.0;
			
			//E-step
			for(int i = 0; i < size; i++)
			{
				x = data[i];
				double p = getProbability(x);//x生成的概率
				System.out.println("p:"+p);
				for (int k = 0; k < m_Ncompo; k++)
				{
					//已知各个compotent的先验、均值、协方差，求gamma_ik
					double gamma_ik =gaussian(x, k) * m_priors[k] / p; // gaussian(x, k) * m_priors[k] / p 第i个数据由第k的compotent生成的概率

					next_priors[k] += gamma_ik;  //每个component的软计数

					for (int d = 0; d < m_Ndim; d++)
					{
						next_means[k][d] += gamma_ik * x[d];//gamma_ik * x[d]
						next_vars[k][d] += gamma_ik * x[d] * x[d]; //gamma_ik * x[d] * x[d]
					}
				}

				currL += (p > 1E-20) ? Math.log(p) : -20;  //(p > 1E-20) ? Math.log(p) : -20 对数似然
			}
			currL /= size;
			
			//M-step
			// Re-estimation: generate new priors, means and variances.
			for (int k = 0; k < m_Ncompo; k++)
			{
				m_priors[k] = next_priors[k] / size; //next_priors[k] / size最大化先验概率

				if (m_priors[k] > 0)
				{
					for (int d = 0; d < m_Ndim; d++)
					{
						m_means[k][d] = next_means[k][d] / next_priors[k];//next_means[k][d] / next_priors[k]
						m_vars[k][d] = next_vars[k][d] / next_priors[k] - m_means[k][d] * m_means[k][d]; //next_vars[k][d] / next_priors[k] - m_means[k][d] * m_means[k][d]
						if (m_vars[k][d] < m_minVars[d])
						{
							m_vars[k][d] = m_minVars[d]; //每个component的协方差不能小于全局协方差*0.01
						}
					}
				}
			}
			// Terminal conditions
			iterNum++;
			if (Math.abs(currL - lastL) < m_endError * Math.abs(lastL))
			{
				unchanged++;
			}
			if (iterNum >= m_maxIter || unchanged >= 3)
			{
				loop = false;
			}
		}
	}
	//-----------------------end--------------------------
	//===================初始化各个component的先验、均值、协方差====================
	public void init(double[][] data, int size)
	{
		final double MIN_VAR = 1e-10;
		
		KMeans kmeans = new KMeans(m_Ndim, m_Ncompo);
		int[] labels = new int[size];
		kmeans.cluster(data, labels, size);
		//输出聚类结果
		System.out.println("K-Means cluster result:");
		for(int i = 0; i < size; i ++){
			//for(int d = 0; d < m_Ndim; d++){
				System.out.print(i+" "); //System.out.print(data[i][d]+" ");
		   // }
			System.out.println("belong to cluster "+ labels[i] );
		}
			
		int[] counts = new int[m_Ncompo];
		double[] overMeans = new double[m_Ndim];
		for(int d = 0; d < m_Ndim; d++)
		{
			overMeans[d] = 0.0;
			m_minVars[d] = 0.0;
		}
		for(int k = 0; k < m_Ncompo; k++)
		{
			counts[k] = 0;
			m_priors[k] = 0;
			for(int d = 0; d < m_Ndim; d++)
			{
				m_means[k][d] = kmeans.getMean(k)[d];
				m_vars[k][d] = 0;
			}
		}
		double[] x = null;
		int label = -1;
		
		for (int i = 0; i < size; i++)
		{
			x = data[i];
			label = labels[i];
			counts[label]++;
			double[] m = kmeans.getMean(label);
			for (int d = 0; d < m_Ndim; d++)
			{
				m_vars[label][d] += (x[d] - m[d]) * (x[d] - m[d]); 
			}
			for (int d = 0; d < m_Ndim; d++)
			{
				overMeans[d] += x[d];
				m_minVars[d] += x[d] * x[d];
			}
		}
		//训练数据的总协方差(* 0.01)作为最小协方差
		double temp_var;
		for (int d = 0; d < m_Ndim; d++)
		{
			overMeans[d] /= size;
			temp_var = 0.01 * (m_minVars[d] / size - overMeans[d] * overMeans[d]);//求方差公式
			m_minVars[d] = temp_var > MIN_VAR ? temp_var : MIN_VAR;
			//m_minVars[d] = max(MIN_VAR, 0.01 * (m_minVars[d] / size - overMeans[d] * overMeans[d]));
		}
		//初始化每个component
		for(int k = 0; k < m_Ncompo; k++)
		{
			m_priors[k] = 1.0 * counts[k] / size;
			if(m_priors[k] > 0)
			{
				for (int d = 0; d < m_Ndim; d++){
					m_vars[k][d] = m_vars[k][d] / counts[k]; //每个component的方差
					// A minimum variance for each dimension is required
					//限定每维上的最小方差
					if (m_vars[k][d] < m_minVars[d])
					{
						m_vars[k][d] = m_minVars[d];
					}
				}
			}
			else
			{
				for (int d = 0; d < m_Ndim; d++)
					m_vars[k][d] = m_minVars[d];
				System.out.println("[WARNING] Gaussian " + k+" of GMM is not used!");
			}
		}
	}
	//------------------------------------end----------------------------------------
	//==============================计算每个样本的概率=================================
	public double getProbability(double[] x)
	{
		double p = 0.0;
		for (int k = 0; k < m_Ncompo; k++)
		{
			p += m_priors[k] * gaussian(x, k);  //m_priors[k] * gaussian(x, k)
		}
		return p;
	}
	public double gaussian(double[] x, int k)
	{
		double p = 1;
		for (int d = 0; d < m_Ndim; d++)
		{
			p *= 1 / Math.sqrt(2 * 3.14159 * m_vars[k][d]); //p *= 1 / Math.sqrt(2 * 3.14159 * m_vars[k][d]);
			p *= Math.exp(-0.5 * (x[d] - m_means[k][d]) * (x[d] - m_means[k][d]) / m_vars[k][d]); //p *= Math.exp(-0.5 * (x[d] - m_means[k][d]) * (x[d] - m_means[k][d]) / m_vars[k][d]);
		}
		return p;
	}
	//----------------------------------end-------------------------------------
	//-------------------------------
	public double getPartialProb(double[] x, int k)
	{
		double gamma_ik =gaussian(x, k) * m_priors[k] / getProbability(x);
		return gamma_ik/*OutputFormat.formatOut(gamma_ik)*/;
	}
}
