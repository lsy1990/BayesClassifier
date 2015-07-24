
package com.jamdeo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

/**
 * 朴素贝叶斯分类器
 */
public class BayesClassifier
{
    private TrainingDataManager tdm;// 训练集管理器
    private String trainnigDataPath;// 训练集路径
    private static double zoomFactor = 10000;

    /**
     * 默认的构造器，初始化训练集
     */
    public BayesClassifier()
    {
        tdm = new TrainingDataManager();
    }

    /**
     * 计算给定的文本属性向量X在给定的分类Cj中的类条件概率 <code>ClassConditionalProbability</code>连乘值
     * 
     * @param X 给定的文本属性向量
     * @param Cj 给定的类别
     * @return 分类条件概率连乘值，即<br>
     */
    double calcProd(String[] X, String Cj)
    {
        float ret = 1.0F;
        // 类条件概率连乘
        for (int i = 0; i < X.length; i++)
        {
            String Xi = X[i];
            // 因为结果过小，因此在连乘之前放大10倍，这对最终结果并无影响，因为我们只是比较概率大小而已
            ret *= ClassConditionalProbability.calculatePxc(Xi, Cj) * zoomFactor;
        }
        // 再乘以先验概率
        ret *= PriorProbability.calculatePc(Cj);
        return ret;
    }

    /**
     * 去掉停用词
     * 
     * @param text 给定的文本
     * @return 去停用词后结果
     */
    public String[] DropStopWords(String[] oldWords)
    {
        Vector<String> v1 = new Vector<String>();
        for (int i = 0; i < oldWords.length; ++i)
        {
            if (StopWordsHandler.IsStopWord(oldWords[i]) == false)
            {// 不是停用词
                v1.add(oldWords[i]);
            }
        }
        String[] newWords = new String[v1.size()];
        v1.toArray(newWords);
        return newWords;
    }

    /**
     * 对给定的文本进行分类
     * 
     * @param text 给定的文本
     * @return 分类结果
     */
    @SuppressWarnings("unchecked")
    public String classify(String text)
    {
        String[] terms = null;
        terms = ChineseSpliter.split(text, " ").split(" ");// 中文分词处理(分词后结果可能还包含有停用词）
        terms = DropStopWords(terms);// 去掉停用词，以免影响分类

        String[] Classes = tdm.getTraningClassifications();// 分类
        double probility = 0.0F;
        List<ClassifyResult> crs = new ArrayList<ClassifyResult>();// 分类结果
        for (int i = 0; i < Classes.length; i++)
        {
            String Ci = Classes[i];// 第i个分类
            probility = calcProd(terms, Ci);// 计算给定的文本属性向量terms在给定的分类Ci中的分类条件概率
            // 保存分类结果
            ClassifyResult cr = new ClassifyResult();
            cr.classification = Ci;// 分类
            cr.probility = probility;// 关键字在分类的条件概率
            System.out.println("In process....");
            System.out.println(Ci + "：" + probility);
            crs.add(cr);
        }
        // 对最后概率结果进行排序
        java.util.Collections.sort(crs, new Comparator()
        {
            public int compare(final Object o1, final Object o2)
            {
                final ClassifyResult m1 = (ClassifyResult) o1;
                final ClassifyResult m2 = (ClassifyResult) o2;
                final double ret = m1.probility - m2.probility;
                if (ret < 0)
                {
                    return 1;
                }
                else
                {
                    return -1;
                }
            }
        });
        // 返回概率最大的分类
        return crs.get(0).classification;
    }

    public static void main(String[] args)
    {
        String text = "法国雅高酒店集团在全球90个国家拥有和管理4000家酒店。目前在中国的酒店品牌有：索菲特，美爵，诺富特，美居，以及宜必思。宜必思是雅高旗下的经济型酒店，>其经营理念是让旅客以经济型的价位享受物超所值的国家标准的酒店住宿";
       // text = "中美关系总体发展良好，两国保持密切交往，各领域务实合作稳步推进，在重大国际、地区和全球性问题上保持密切沟通和协调。在双方共同努力下，第七轮中美战略与经济对话和第六轮中美人文交流高层磋商取得重要成果。应总统先生邀请，我将于9月对美国进行国事访问。我愿同你一道努力，通过访问增进两国人民相互了解和友谊，扩大双方各领域合作";
       text = "美国知名博彩网站Bovada公布了更新一期NBA各队下赛季的夺冠赔率榜。在这份榜单中，骑士依然高居首位，而火箭在通过交易获得泰-劳森之后，排名升至第6。不过颇有些令人意外的是，今夏刚刚补充了林书豪等多名战将的黄蜂，却极度被外界看衰，他们与76人、魔术并列排在了这份榜单的倒数第一位。";
        BayesClassifier classifier = new BayesClassifier();// 构造Bayes分类器
        String result = classifier.classify(text);// 进行分类
        System.out.println("此项属于[" + result + "]");
    }
}
