package eDLineEditor;

import java.util.ArrayList;
import java.util.HashMap;

public class Data {// 储存数据类
	protected static int currentLine = 0;// 当前行
	protected static int mode = 0;// 命令模式为0， 输入模式为1
	protected static int qCount = 0;// 计数q命令输入次数
	protected static boolean isSaved = true;// 判断文本修改的内容是否保存到文件，起始状态为已保存
	protected static boolean isSetName = false;// 判断文件是否已设置默认文件名
	protected static String defaultFileName = "";// 默认文件名
	protected static HashMap<Character, String> signLines = new HashMap<Character, String>();// 保存标记及对应行
	protected static ArrayList<String> current = new ArrayList<String>();// 单步操作文件缓存
	protected static ArrayList<ArrayList<String>> cache = new ArrayList<ArrayList<String>>();// 缓存区
	protected static ArrayList<Integer> pointerList = new ArrayList<Integer>();// 保存每次改变文本时的当前行
}
