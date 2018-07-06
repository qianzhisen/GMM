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
	private double[][] m_means; //ÿһ�д�ž�ֵ����
	private double[][] m_vars;  //ÿһ�д�ŶԽ�Э����
	
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
			//���顢��ֵ��Э������0
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
				double p = getProbability(x);//x���ɵĸ���
				System.out.println("p:"+p);
				for (int k = 0; k < m_Ncompo; k++)
				{
					//��֪����compotent�����顢��ֵ��Э�����gamma_ik
					double gamma_ik =gaussian(x, k) * m_priors[k] / p; // gaussian(x, k) * m_priors[k] / p ��i�������ɵ�k��compotent���ɵĸ���

					next_priors[k] += gamma_ik;  //ÿ��component�������

					for (int d = 0; d < m_Ndim; d++)
					{
						next_means[k][d] += gamma_ik * x[d];//gamma_ik * x[d]
						next_vars[k][d] += gamma_ik * x[d] * x[d]; //gamma_ik * x[d] * x[d]
					}
				}

				currL += (p > 1E-20) ? Math.log(p) : -20;  //(p > 1E-20) ? Math.log(p) : -20 ������Ȼ
			}
			currL /= size;
			
			//M-step
			// Re-estimation: generate new priors, means and variances.
			for (int k = 0; k < m_Ncompo; k++)
			{
				m_priors[k] = next_priors[k] / size; //next_priors[k] / size����������

				if (m_priors[k] > 0)
				{
					for (int d = 0; d < m_Ndim; d++)
					{
						m_means[k][d] = next_means[k][d] / next_priors[k];//next_means[k][d] / next_priors[k]
						m_vars[k][d] = next_vars[k][d] / next_priors[k] - m_means[k][d] * m_means[k][d]; //next_vars[k][d] / next_priors[k] - m_means[k][d] * m_means[k][d]
						if (m_vars[k][d] < m_minVars[d])
						{
							m_vars[k][d] = m_minVars[d]; //ÿ��component��Э�����С��ȫ��Э����*0.01
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
	//===================��ʼ������component�����顢��ֵ��Э����====================
	public void init(double[][] data, int size)
	{
		final double MIN_VAR = 1e-10;
		
		KMeans kmeans = new KMeans(m_Ndim, m_Ncompo);
		int[] labels = new int[size];
		kmeans.cluster(data, labels, size);
		//���������
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
		//ѵ�����ݵ���Э����(* 0.01)��Ϊ��СЭ����
		double temp_var;
		for (int d = 0; d < m_Ndim; d++)
		{
			overMeans[d] /= size;
			temp_var = 0.01 * (m_minVars[d] / size - overMeans[d] * overMeans[d]);//�󷽲ʽ
			m_minVars[d] = temp_var > MIN_VAR ? temp_var : MIN_VAR;
			//m_minVars[d] = max(MIN_VAR, 0.01 * (m_minVars[d] / size - overMeans[d] * overMeans[d]));
		}
		//��ʼ��ÿ��component
		for(int k = 0; k < m_Ncompo; k++)
		{
			m_priors[k] = 1.0 * counts[k] / size;
			if(m_priors[k] > 0)
			{
				for (int d = 0; d < m_Ndim; d++){
					m_vars[k][d] = m_vars[k][d] / counts[k]; //ÿ��component�ķ���
					// A minimum variance for each dimension is required
					//�޶�ÿά�ϵ���С����
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
	//==============================����ÿ�������ĸ���=================================
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
