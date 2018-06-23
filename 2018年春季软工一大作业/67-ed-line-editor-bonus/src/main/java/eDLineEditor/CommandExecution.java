package eDLineEditor;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandExecution {
	static String paramString = ""; // s命令后面的参数，设为静态，以保证重复调用上一次的参数
	static String lineBreak = System.getProperty("line.separator"); // 换行符
	
	// 辅助方法[1] 实现退出之后的清零
		public static void reSet() {
			Data.current = new ArrayList<String>();
			Data.currentLine = 0;
			Data.defaultFileName = null;
			Data.isSaved = true;
			Data.isSetName = false;
			Data.mode = 0;
			Data.signLines = new HashMap<Character, String>();
			Data.pointerList = new ArrayList<Integer>();
		}
		
	// 辅助方法[2] 判断两个文件版本内容是否相同
	public static void addToCache() {
		if (isChangedContent(Data.current, Data.cache.get(Data.cache.size() - 1))) {
			// 一定要new 一个ArrayList，否则只是存入一个静态变量current的引用，导致缓存区cache的大小没有改变
			Data.cache.add(new ArrayList<String>(Data.current));
			Data.pointerList.add(Data.currentLine);// Integer类型的ArrayList只是存入一个new Integer对象，不受影响
			Data.isSaved = false;// 保存状态改变
		}
	}

	// 辅助方法[3] 比较文本是否改变
	public static boolean isChangedContent(ArrayList<String> list1, ArrayList<String> list2) {
		if (list1.size() != list2.size()) {
			return true;
		} else {
			for (int i = 0; i < list1.size(); i++) {
				if (!list2.get(i).equals(list1.get(i))) {
					return true;
				}
			}
			return false;
		}
	}

	// 辅助方法[4] 实现输入到编辑器功能及模式转换，前三个命令共有
	public static void input(String content) {
		if (!content.equals(".")) {
			Data.current.add(Data.currentLine, content);
			Data.currentLine++;
			Data.isSaved = false;
		} else {// 输入 . 时，退出输入模式
			Data.mode = 0;
			// 退出输入模式时，检验是否进行了文本修改，若进行了修改，则加入缓存区，否则不加入
			addToCache();
			Data.pointerList.remove(Data.pointerList.size() - 1);
			Data.pointerList.add(Data.currentLine); // 栈顶更新
		}
	}

	// [1] 切换到输入模式，将新输入的文本追加到指定行的后面，当前行被设为输入文本的最后一行: a
	public static void addingBehind(int[] address) {
		Data.currentLine = address[0];
		Data.mode = 1;
	}

	// [2] 切换到输入模式，将新输入的文本追加到指定行的前面，当前行被设为输入文本的最后一行: i
	public static void addingBefore(int address[]) {
		Data.currentLine = (address[0] - 1) >= 0 ? (address[0] - 1) : 0;// 经验证，地址可以为0
		Data.mode = 1;
	}

	// [3] 切换到输入模式，将新输入的文本替换成指定行，当前行被设为输入文本的最后一行: c
	public static void covering(int[] address) {
		// 删去指定行内容
		address[0]--;
		// remove操作的index为实际行数-1
		int count = address[0];
		while (count < address[1]) {
			Data.current.remove(address[0]);
			count++;
		}
		Data.currentLine = (address[0] >= 0) ? address[0] : 0;// 变为实际行数
		Data.mode = 1;
	}

	// [4] 删除指定行，如果被删除的文本后还有文本行，则当前行被设为该行，否则设为被删除的文本的上一行: d
	public static void delete(int[] address) {
		address[0]--;
		// remove操作的index为实际行数-1
		int count = address[0];
		while (count < address[1]) {
			Data.current.remove(address[0]);
			count++;
		}
		address[0]++;// 变为实际行数
		if (Data.current.size() >= address[0]) {
			Data.currentLine = address[0];
		} else {
			Data.currentLine = address[0] - 1;
		}
		addToCache();
	}

	// [5] 打印指定行的内容，当前行被设为打印行的最后一行: p
	public static void printContent(int[] address) {
		for (int i = address[0] - 1; i < address[1]; i++) {
			System.out.println(Data.current.get(i));
		}
		Data.currentLine = address[1];
		Data.pointerList.remove(Data.pointerList.size() - 1);
		Data.pointerList.add(Data.currentLine); // 栈顶更新
	}

	// [6] 打印指定行的行号: = 不改变当前行
	public static void printIndex(int[] address) {
		System.out.println(address[0]);
	}

	/**
	 * [7] (.+1)z[n] 从指定行，一次向后移动n行，打印包含指定行以及其后n行的内容。当前行被设为最后被打印的行。
	 * 当指定行到末尾行不足n行时，打印当前行到末尾行。当参数n指定时，必为正整数；不指定时，打印当前行到末尾行。
	 */
	public static void printBackward(int[] address, int n) {
		int count = address[0] - 1;
		while ((count < (address[0] + n)) && (count < Data.current.size())) {
			System.out.println(Data.current.get(count));
			count++;
		}
		Data.currentLine = count + 1;
		Data.pointerList.remove(Data.pointerList.size() - 1);
		Data.pointerList.add(Data.currentLine); // 栈顶更新
	}

	/**
	 * [8] f [file] 设置默认文件名，不打印任何内容。 如果没有指定参数file，则打印默认文件名（没有默认文件名时，打印'?'）。
	 * 通过edfile命令进入时，file被设置为默认文件名。通过ed命令进入时，没有默认文件名。
	 */
	public static void setFileName(String newFileName) {
		if (newFileName == null) {
			System.out.println(Data.defaultFileName);
		} else {
			Data.defaultFileName = newFileName;
			Data.isSetName = true;
		}
	}

	/**
	 * [9] (1,$)w [file] 保存指定行到指定文件，当前行不变。保存后不需要输出或打印任何内容。
	 * 如果指定文件存在，则保存的内容覆盖文件内容。如果文件不存在，则创建文件并保存内容。 如果参数file不指定，则使用默认文件名代替参数file。
	 * 当没有默认文件名时，参数file必须指定，否则打印'?'提示。注意，w的file参数不会更改默认文件名。
	 */
	@SuppressWarnings("resource")
	public static void overWriteFile(int[] address, String newFileName) {
		try {
			FileWriter fw = new FileWriter(Data.defaultFileName, false);
			if (!newFileName.equals("")) {
				fw = new FileWriter(newFileName, false);
			}
			if (address[0] == 1 && address[1] == Data.current.size()) {
				Data.isSaved = true;// 只有当全部行保存时才改变状态
			}
			String resultString = "";
			for (int i = address[0] - 1; i >= 0 && i < address[1]; i++) {
				resultString += (Data.current.get(i) + lineBreak); // 换行符的选用
			}
			fw.write(resultString);
			fw.close();
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	/**
	 * [10] (1,$)W [file] 保存指定行到指定文件，当前行不变。保存后不需要输出或打印任何内容。保存形式为追加保存，追加到文件末尾。
	 * 如果文件不存在，则创建文件并保存内容。其他使用情况参照w命令中的描述
	 */
	@SuppressWarnings("resource")
	public static void addWriteFile(int[] address, String newFileName) {
		try {
			FileWriter fw = new FileWriter(Data.defaultFileName, true);
			if (!newFileName.equals("")) {
				fw = new FileWriter(newFileName, true);
			}
			if (address[0] == 1 && address[1] == Data.current.size()) {
				Data.isSaved = true;// 只有当文件全部被保存的时候才会改变状态
			}
			String resultString = "";
			for (int i = address[0] - 1; i >= 0 && i < address[1]; i++) {
				resultString += (Data.current.get(i) + lineBreak); // 换行符的选用
			}
			fw.write(resultString);
			fw.close();
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	// [11] (.,.)m(.) 移动左边源指定行到右边目的指定行（单行）后，当前行被设为移动行的最后一行
	public static void cutMove(int[] address, int directAddress) {
		// 删去指定行内容
		address[0]--;
		// remove操作的index为(实际行数-1)
		int count = address[0];
		int i = 0;
		String[] tempStr = new String[address[1] - address[0]];// 记录左边源制定行内容
		while (count < address[1]) {
			tempStr[i] = Data.current.get(address[0]);
			Data.current.remove(address[0]);
			count++;
			i++;
		}
		if (directAddress >= address[1]) {
			directAddress -= i;// 当目标行在源行后面的时候，随着删去行，directAddress也减少，否则不受影响
		}
		Data.currentLine = directAddress;
		for (String aString : tempStr) {
			if (aString != null && !aString.equals("")) {
				Data.current.add(Data.currentLine, aString);
				Data.currentLine++;
			}
		}
		addToCache(); // m 命令也许不改变内容但当前行也许改变了（如当前行=6时，1,2m2后，内容不变但当前行=2）
		Data.pointerList.remove(Data.pointerList.size() - 1);
		Data.pointerList.add(Data.currentLine); // 栈顶更新
	}

	// [12] (.,.)t(.) 复制左边源指定行到右边目的指定行后，当前行被设为复制行的最后一行
	public static void copyMove(int[] address, int directAddress) {
		String temp[] = new String[address[1] - address[0] + 1];
		for (int i = address[0] - 1; i < address[1]; i++) {
			temp[i - address[0] + 1] = Data.current.get(i);
		}
		Data.currentLine = directAddress;
		for (String aString : temp) {
			if (aString != null && !aString.equals("")) {
				Data.current.add(Data.currentLine, aString);
				Data.currentLine++;
			}
		}
		addToCache();
	}

	// [13] j 合并指定行内容，当前行被设为合并行
	public static void join(int[] address) {
		// 删去指定行内容
		address[0]--;
		// remove操作的index为(实际行数-1)
		int count = address[0];
		int i = 0;
		String[] temp = new String[address[1] - address[0]];// 记录指定行内容
		while (count < address[1]) {
			temp[i] = Data.current.get(address[0]);
			Data.current.remove(address[0]);
			count++;
			i++;
		}
		Data.currentLine = address[0] + 1;
		StringBuilder sb = new StringBuilder();
		for (String aString : temp) {
			if (aString != null && !aString.equals("")) {
				sb.append(aString);
			}
		}
		Data.current.add(Data.currentLine - 1, sb.toString());
		addToCache();// j 命令也许不会改变内容但改变当前行（如1,1j）
		Data.pointerList.remove(Data.pointerList.size() - 1);
		Data.pointerList.add(Data.currentLine); // 栈顶更新
	}

	/**
	 * [14] (.,.)s[/str1/str2/count] (.,.)s[/str1/str2/g] (.,.)s
	 * 在指定行用str2替换str1。当最后为count（正整数）时，对指定的每一行，替换第count个匹配的str1（若该行没有，则不替换），
	 * 若不指定，即(.,.)s/str1/str2/，默认每一行替换第一个。 当最后为g时，替换所有。当指定行没有符合要求的或没有第count个时，输出'?'。
	 * (.,.)s重复上一次替换命令，当前行被设为最后一个被改变的行。
	 */
	public static boolean substitute(int[] address, String param) {
		// 先删去指定行内容，然后再把替换后的内容放回文本
		address[0]--;
		// remove操作的index为(实际行数-1)
		int count = address[0];
		String[] tempString = new String[address[1] - address[0]];// 记录左边源制定行内容使其实现随后的替换操作
		while (count < address[1]) {
			tempString[count - address[0]] = Data.current.get(address[0]);
			Data.current.remove(address[0]);
			count++;
		}
		// ------删去部分没问题------
		if (!param.equals("")) {
			paramString = param;
		} else {
			param = paramString;
		}
		Pattern p = Pattern.compile("(/([^/]+)/([^/]*)/(g|\\d+)?)?"); // 参数部分的正则表达式，str1非空，str2可空
		Matcher m = p.matcher(param);
		String str1 = "", str2 = "";
		if (m.find()) {// 必须用find()才能把group()赋值给str1,str2
			str1 = m.group(2).toString();
			str2 = m.group(3).toString();
		}
		String posibleParam = param.substring(param.lastIndexOf('/') + 1);
		boolean isSubstituted = false; // 记录每行是否替换
		Pattern pattern = Pattern.compile(str1);
		Matcher matcher;
		for (int i = 0; i < (address[1] - address[0]); i++) {
			String linShi = tempString[i];
			// 处理
			matcher = pattern.matcher(tempString[i]);
			if (posibleParam.equals("g")) {
				tempString[i] = tempString[i].replaceAll(str1, str2);// 如果末尾为g则替换所有
			} else if (posibleParam.equals("")) {
				tempString[i] = tempString[i].replaceFirst(str1, str2);// 如果末尾为/则替换第一个
			} else {
				int j = 0;// 计数器
				int sCount = Integer.parseInt(posibleParam);// 记录参数count
				if (sCount == 0) {
					return false;
				}
				StringBuffer buf = new StringBuffer();
				while (matcher.find()) {
					j++;
					if (j == sCount) {// 当计数器达到count时，进行下一项操作
						matcher.appendReplacement(buf, str2);
						break;
					}
				}
				matcher.appendTail(buf);
				tempString[i] = buf.toString();
			}
			if (!tempString[i].equals(linShi)) {
				Data.currentLine = i + address[0] + 1; // 当前行是最后发生替换的行号
				isSubstituted = true;// 如果确实替换了的话，那么替换合法
			}
		}
		if (!isSubstituted) {
			// 如果指定行没有符合要求的或没有第count个，那么打?号，而且不执行这次命令
			Data.current = new ArrayList<String>(Data.cache.get(Data.cache.size() - 1));
			Data.currentLine = Data.pointerList.get(Data.pointerList.size() - 1);
			return false;
		}
		for (String aStr : tempString) {
			Data.current.add(address[0], aStr);
			address[0]++;
		}
		addToCache();// s命令也许不改变内容但当前行也许改变了（如当前行=6，1,2s/a/a/count后，内容不变但当前行=2）
		Data.pointerList.remove(Data.pointerList.size() - 1);
		Data.pointerList.add(Data.currentLine); // 栈顶更新
		return true;
	}

	/**
	 * [15] (.)k[x] 用一个小写字符'x'标记指定行，标记成功后，可以通过"'x"这个地址（英文单引号'加上字符x）来访问该行。
	 * 当被标记行被删除或修改后，该标记失效。在被标记行前后插入内容，或者移动被标记行时，该标记不会失效。 可以对一行加多个标记，一个标记只能标记一行。
	 */
	public static void stamp(int[] address, char symbol) {
		Data.signLines.put(symbol, Data.current.get(address[0] - 1));
	}

	/**
	 * [16] 撤销上一次改动文本的命令，当前地址被设为撤销改动后的最后一行。
	 * 注意，撤销的是对编辑器缓存中文本的改动，写入文件后，之前的改动不可撤销。可撤销多次。
	 */
	public static void cancel() {
		Data.cache.remove(Data.cache.size() - 1);// 将本次的版本移除
		Data.pointerList.remove(Data.pointerList.size() - 1);
		Data.current = new ArrayList<String>(Data.cache.get(Data.cache.size() - 1));// 将文本变为上一步的版本
		Data.currentLine = Data.pointerList.get(Data.pointerList.size() - 1);// 当前行也变为上一次的版本
	}

}