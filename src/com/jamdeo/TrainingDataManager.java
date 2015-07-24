
package com.jamdeo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 训练集管理器
 */

public class TrainingDataManager
{
    private String[] traningFileClassifications;// 训练语料分类集合
    private File traningTextDir;// 训练语料存放目录
    private static String defaultPath = "/home/lsy/opensource/Reduced";

    HashMap<String, String[]> trainingSrcData = new LinkedHashMap<String, String[]>();
    HashMap<String, Integer> trainingWordsFreqHight = null;

    public TrainingDataManager()
    {
        traningTextDir = new File(defaultPath);
        if (!traningTextDir.isDirectory())
        {
            throw new IllegalArgumentException("训练语料库搜索失败！ [" + defaultPath + "]");
        }
        this.traningFileClassifications = traningTextDir.list();
    }

    /**
     * 返回训练文本类别，这个类别就是目录名
     * 
     * @return 训练文本类别
     */
    public String[] getTraningClassifications()
    {
        return this.traningFileClassifications;
    }

    /**
     * 返回训练文本类别，这个类别就是目录名
     * 
     * @return 训练文本类别
     */
    public int getTraningWordsClassificationsCount()
    {
        if (trainingWordsFreqHight == null) {
            trainingWordsFreqHight = new LinkedHashMap<String, Integer>();
            for (int i = 0; i < traningFileClassifications.length; i++)
            {
                String[] strWords = getTrainingWordsOfClassification(traningFileClassifications[i]);
                for (int j = 0; j < strWords.length; j++) {
                    if (!trainingWordsFreqHight.containsKey(strWords[j])) {
                        trainingWordsFreqHight.put(strWords[j], 1);
                    } else {
                        trainingWordsFreqHight.put(strWords[j],
                                trainingWordsFreqHight.get(strWords[j]) + 1);
                    }
                }
            }
        }

        return trainingWordsFreqHight.keySet().size();
    }

    /**
     * 根据训练文本类别返回这个类别下的所有训练文本路径（full path）
     * 
     * @param classification 给定的分类
     * @return 给定分类下所有文件的路径（full path）
     */
    public String[] getFilesPath(String classification)
    {
        File classDir = new File(traningTextDir.getPath() + File.separator + classification);
        String[] ret = classDir.list();
        for (int i = 0; i < ret.length; i++)
        {
            ret[i] = traningTextDir.getPath() + File.separator + classification + File.separator
                    + ret[i];
        }
        return ret;
    }

    /**
     * 返回给定路径的文本文件内容
     * 
     * @param filePath 给定的文本文件路径
     * @return 文本内容
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public static String getText(String filePath) throws FileNotFoundException, IOException
    {

        InputStreamReader isReader = new InputStreamReader(new FileInputStream(filePath), "GBK");
        BufferedReader reader = new BufferedReader(isReader);
        String aline;
        StringBuilder sb = new StringBuilder();

        while ((aline = reader.readLine()) != null)
        {
            sb.append(aline + " ");
        }
        isReader.close();
        reader.close();
        return sb.toString();
    }

    /**
     * 返回训练文本集中所有的文本数目
     * 
     * @return 训练文本集中所有的文本数目
     */
    public int getTrainingWordsCount()
    {
        int ret = 0;
        for (int i = 0; i < traningFileClassifications.length; i++)
        {
            ret += getTrainingWordsCountOfClassification(traningFileClassifications[i]);
        }
        return ret;
    }

    /**
     * 返回训练文本集中在给定分类下的训练文本数目
     * 
     * @param classification 给定的分类
     * @return 训练文本集中在给定分类下的训练文本数目
     */
    public int getTrainingWordsCountOfClassification(String classification)
    {
        String[] strWords = getTrainingWordsOfClassification(classification);
        return strWords.length;
    }

    /**
     * 返回给定分类中包含关键字／词的训练文本的数目
     * 
     * @param classification 给定的分类
     * @param key 给定的关键字／词
     * @return 给定分类中包含关键字／词的训练文本的数目
     */
    public int getCountContainKeyOfClassification(String classification, String key)
    {
        int ret = 0;

        String[] strWords = getTrainingWordsOfClassification(classification);
        for (int j = 0; j < strWords.length; j++)
        {
            if (strWords[j].equals(key))
            {
                ret++;
            }
        }

        return ret;
    }

    /**
     * 返回给定分类原始数据数组
     * 
     * @param classification 给定的分类
     * @return 给定分类原始词组
     */
    public String[] getTrainingWordsOfClassification(String classification)
    {
        if (trainingSrcData.containsKey(classification)) {
            return trainingSrcData.get(classification);
        }

        Vector<String> v1 = new Vector<String>();
        try
        {
            String[] filePath = getFilesPath(classification);
            for (int j = 0; j < filePath.length; j++)
            {
                String text = getText(filePath[j]);
                String[] terms = null;
                terms = ChineseSpliter.split(text, " ").split(" ");// 中文分词处理(分词后结果可能还包含有停用词）
                terms = DropStopWords(terms);// 去掉停用词，以免影响分类
                if (terms != null) {
                    for (int i = 0; i < terms.length; ++i)
                    {
                        v1.add(terms[i]);
                    }
                }
            }
        } catch (FileNotFoundException ex)
        {
            Logger.getLogger(TrainingDataManager.class.getName()).log(Level.SEVERE, null, ex);

        } catch (IOException ex)
        {
            Logger.getLogger(TrainingDataManager.class.getName()).log(Level.SEVERE, null, ex);

        }
        String[] newWords = new String[v1.size()];
        v1.toArray(newWords);
        v1.clear();
        trainingSrcData.put(classification, newWords);
        return newWords;
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
        v1.clear();
        return newWords;
    }
}
