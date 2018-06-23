package eDLineEditor;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandInterpretation {

	public void startCommand() {
		String order = "ED";// 起始命令
		Scanner in = new Scanner(System.in);
		while ((order = in.nextLine()) != null) {
			if (!order.startsWith("ed")) {
				System.out.println(order + ": command not found");
			} else {
				break;
			}
		}
		// 进入命令的两种方法
		CommandExecution.reSet();// 文本内容初始化
		if (order.length() > 3) {
			String nextLine = null;
			// 读取ed file 命令中file部分的名字,并记录为默认文件名，isSave为true
			Data.defaultFileName = order.substring(3, order.length());
			Data.isSetName = true;
			File file = new File(Data.defaultFileName);
			try {
				FileWriter fileWriter = new FileWriter(file, true);// 如果文件不存在则自动创建
				fileWriter.close();
				BufferedReader bf = new BufferedReader(new FileReader(file));
				while ((nextLine = bf.readLine()) != null) {
					if (!nextLine.equals("") && !nextLine.equals(" ") && !nextLine.equals("\n")) {
						Data.current.add(nextLine);
					}
				}
				bf.close();
			} catch (FileNotFoundException e) {
				// TODO: handle exception
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Data.currentLine = Data.current.size();// 当前行为最后一行
		Data.cache.add(new ArrayList<String>(Data.current));// 初始文本内容加入缓存区
		Data.pointerList.add(Data.currentLine);

		// 命令模式开始解析
		while ((order = in.nextLine()) != null) {
			// Q 命令，不论有没有更改未保存，都不提示直接退出ed
			if (order.equals("Q") && Data.mode == 0) {
				return;
			}
			// q 命令
			else if (order.equals("q") && Data.mode == 0) {
				Data.qCount++;
				// 当文件被保存或者连续输入两次q，确认退出
				if (Data.isSaved || Data.qCount == 2) {
					return;
				}
				// 当有更改的内容未保存到文件时，提示一次'?'
				System.out.println("?");
			} else {
				resolveCommand(order);
				Data.qCount = 0;
			}
		}
		in.close();
	}

	// 判断命令类型并执行相应命令
	public void resolveCommand(String order) {
		int[] address = new int[2];// 地址
		String filename = "";// 文件名
		int[] defaultAddresses = { Data.currentLine, Data.currentLine };// 默认地址
		int directAddress;// m/t命令的目的地址
		if (Data.mode == 0) {// 命令模式
			// 首先判断 命令输入是否规范
			if (order.equals(queryAddress(order))) {
				System.out.println("?");
				return;
			}
			// 取出地址位后紧接着的命令标识符
			switch (order.charAt(queryAddress(order).length())) {

			// [1] a 命令
			case 'a':
				defaultAddresses[0] = defaultAddresses[1] = Data.currentLine;// a的默认地址
				if (order.equals("a")) {
					address = defaultAddresses;
				} else {
					address = getAddress(order);
				}
				if (!isInputValid(address)) {
					return;
				}
				CommandExecution.addingBehind(address);
				break;

			// [2] i 命令
			case 'i':
				defaultAddresses[0] = defaultAddresses[1] = Data.currentLine;// i的默认地址
				if (order.equals("i")) {
					address = defaultAddresses;
				} else {
					address = getAddress(order);
				}
				if (!isInputValid(address)) {
					return;
				}
				CommandExecution.addingBefore(address);
				break;

			// [3] c 命令
			case 'c':
				defaultAddresses[0] = defaultAddresses[1] = Data.currentLine;// i的默认地址
				if (order.equals("c")) {
					address = defaultAddresses;
				} else {
					address = getAddress(order);
				}
				if (!isValidAddress(address)) {
					return;
				}
				CommandExecution.covering(address);
				break;

			// [4] d 命令
			case 'd':
				defaultAddresses[0] = defaultAddresses[1] = Data.currentLine;// d的默认地址
				if (order.equals("d")) {
					address = defaultAddresses;
				} else {
					address = getAddress(order);
				}
				if (!isValidAddress(address)) {
					return;
				}
				CommandExecution.delete(address);
				break;

			// [5] p 命令
			case 'p':
				defaultAddresses[0] = defaultAddresses[1] = Data.currentLine;// p的默认地址
				if (order.equals("p")) {
					address = defaultAddresses;
				} else {
					address = getAddress(order);
				}
				if (!isValidAddress(address)) {
					return;
				}
				CommandExecution.printContent(address);
				break;

			// [6] = 命令
			case '=':
				defaultAddresses[0] = defaultAddresses[1] = Data.current.size();// =的默认地址
				if (order.equals("=")) {
					address = defaultAddresses;
				} else {
					address = getAddress(order);
				}
				if (!isValidAddress(address)) {
					return;
				}
				CommandExecution.printIndex(address);
				break;

			// [7] z 命令
			case 'z':
				int defaultAdd = Data.currentLine + 1;// z的默认地址
				int n;// 打印行数
				try {
					if (order.startsWith("z")) {
						address[0] = address[1] = defaultAdd;
						if (order.equals("z")) {
							n = Data.current.size() - address[0];// 使得 n+ address[0]=Data.current.size()
						} else {
							n = Integer.parseInt(order.substring(1));
						}
					} else {
						String num = order.substring(queryAddress(order).length() + 1);// 取出数字部分
						address = getAddress(order);
						if (order.endsWith("z")) {
							n = Data.current.size() - address[0];
						} else {
							n = Integer.parseInt(num);
						}
					}
				} catch (NumberFormatException e) {
					// TODO: handle exception
					System.out.println("?");
					return;
				}
				if (!isValidAddress(address)) {
					return;
				}
				CommandExecution.printBackward(address, n);
				break;

			// [8] f 命令
			case 'f':
				if (order.equals("f") && Data.isSetName) {
					CommandExecution.setFileName(null);
				} else if (order.length() > 2 && order.charAt(1) == ' ') {
					CommandExecution.setFileName(order.substring(2));
				} else {
					System.out.println("?");
					return;
				}
				break;

			// [9] w 命令
			case 'w':
				defaultAddresses[0] = 1;
				defaultAddresses[1] = Data.current.size();// w的默认地址
				if (order.startsWith("w")) {
					address = defaultAddresses;
					if (order.equals("w")) {
						if (!Data.isSetName) {// 判断是否设置默认文件名
							System.out.println("?");
							return;
						}
					} else {
						filename = order.substring(2);
					}
				} else {
					String tem = order.substring(queryAddress(order).length());// 从地址位开始截取的字符串，同理操作
					address = getAddress(order);
					if (tem.equals("w")) {
						if (!Data.isSetName) {// 判断是否设置默认文件名
							System.out.println("?");
							return;
						}
					} else {
						filename = tem.substring(2);
					}
				}
				if (!isInputValid(address)) {// w/W命令均可写入空字符
					System.out.println("?");
					return;
				}
				CommandExecution.overWriteFile(address, filename);
				break;

			// [10] W 命令
			case 'W':
				defaultAddresses[0] = 1;
				defaultAddresses[1] = Data.current.size();// w的默认地址
				if (order.startsWith("W")) {
					address = defaultAddresses;
					if (order.equals("W")) {
						if (!Data.isSetName) {// 判断是否设置默认文件名
							System.out.println("?");
							return;
						}
					} else {
						filename = order.substring(2);
					}
				} else {
					String tem = order.substring(queryAddress(order).length());// 从地址位开始截取的字符串，同理操作
					address = getAddress(order);
					if (tem.equals("W")) {
						if (!Data.isSetName) {// 判断是否设置默认文件名
							System.out.println("?");
							return;
						}
					} else {
						filename = tem.substring(2);
					}
				}
				if (!isInputValid(address)) {
					System.out.println("?");
					return;
				}
				CommandExecution.addWriteFile(address, filename);
				break;

			// [11] m 命令
			case 'm':
				defaultAddresses[0] = defaultAddresses[1] = Data.currentLine;// m的默认地址
				if (order.startsWith("m")) {
					address = defaultAddresses;
				} else {
					address = getAddress(order);
				}
				String dic = order.substring(queryAddress(order).length() + 1);// 取出目的地址部分
				directAddress = dic.equals("") ? Data.currentLine : singleAddress(dic);// 如果m后面没有地址则用默认地址
				if (!isValidAddress(address)) {
					return;
				} // m命令目标行不能在源行中间
				else if (!(directAddress > 0 && directAddress <= Data.current.size())
						|| (address[0] < directAddress && directAddress < address[1])) {
					System.out.println("?");
					return;
				}
				CommandExecution.cutMove(address, directAddress);
				break;

			// [12] t 命令
			case 't':
				defaultAddresses[0] = defaultAddresses[1] = Data.currentLine;// t的默认地址
				if (order.startsWith("t")) {
					address = defaultAddresses;
				} else {
					address = getAddress(order);
				}
				String dir = order.substring(queryAddress(order).length() + 1);// 取出目的地址部分
				directAddress = dir.equals("") ? Data.currentLine : singleAddress(dir);// 如果t后面没有地址则用默认地址
				if (!isValidAddress(address)) {// t 命令目标行能在源行中间
					return;
				} else if (!(directAddress > 0 && directAddress <= Data.current.size())) {
					System.out.println("?");
					return;
				}
				CommandExecution.copyMove(address, directAddress);
				break;

			// [13] j 命令
			case 'j':
				defaultAddresses[0] = Data.currentLine;
				defaultAddresses[1] = Data.currentLine + 1;// j命令的默认地址
				if (order.endsWith("j")) {
					if (order.equals("j")) {
						address = defaultAddresses;
					} else {
						address = getAddress(order);
					}
				} else {
					System.out.println("?");
					return;
				}
				if (!isInputValid(address)) {
					return;
				}
				CommandExecution.join(address);
				break;

			// [14] s 命令
			case 's':
				defaultAddresses[0] = defaultAddresses[1] = Data.currentLine;
				if (order.startsWith("s")) {
					address = defaultAddresses;
				} else {
					address = getAddress(order);
				}
				if (!isValidAddress(address)) {
					return;
				}
				// 如果之前没有用过s命令，但这次用了s不加字符串的命令，那么报错
				else if (order.endsWith("s") && CommandExecution.paramString.equals("")) {
					System.out.println("?");
					return;
				} else if (!order.substring(queryAddress(order).length() + 1)
						.matches("(/([^/]+)/([^/]*)/(g|\\d+)?)?")) {
					System.out.println("?");// 如果格式不符合要求也报错
					return;
				} else if (!CommandExecution.substitute(address, order.substring(queryAddress(order).length() + 1))) {
					System.out.println("?");
					return;
				}
				break;

			// [15] k 命令
			case 'k':
				defaultAddresses[0] = defaultAddresses[1] = Data.currentLine;
				String symbol = order.substring(queryAddress(order).length() + 1); // 取出k后面的标记部分
				if (order.startsWith("k")) {
					address = defaultAddresses;
				} else {
					address = getAddress(order);// 获得k标记的地址
				}
				if (!isValidAddress(address)) {
					return;
				} else if (!symbol.matches("[a-z]")) {
					System.out.println("?");
					return;
				}
				CommandExecution.stamp(address, order.charAt(order.length() - 1));
				break;

			// [16] u 命令（记得不能撤销w命令，而且撤销改变文本的操作后，q能直接退出）
			case 'u':
				if (Data.cache.size() > 1) {
					CommandExecution.cancel();
				} else {
					Data.isSaved = true; // 当撤销回到最初版本时，文件视为无改动，可直接退出
					System.out.println("?");
					return;
				}
				break;
			default:
				// 若都不能匹配到命令识别符，则报错
				System.out.println("?");
				return;
			}
		} else {// 输入模式下输入文本
			CommandExecution.input(order);
		}
	}

	// 解析地址
	public static int[] getAddress(String order) {
		int[] address = { Data.currentLine, Data.currentLine };
		String place = queryAddress(order);
		if (!place.contains(",")) {
			if (place.equals(";")) {
				address[0] = Data.currentLine;
				address[1] = Data.current.size();
			} else {
				address[0] = address[1] = singleAddress(place);
			}
		} else {
			if (place.equals(",")) {
				address[0] = 1;
				address[1] = Data.current.size();
			} else if (place.charAt(place.length() - 1) == ',') {
				address[0] = singleAddress(place);
				address[1] = Data.currentLine;
			} else if (place.charAt(0) == ',') {// ,(address)型地址
				address[0] = Data.currentLine;
				address[1] = singleAddress(place);
			} else {
				if (place.matches("(\\?[^\\?]*,[\\?]*\\?|/[^/]*,[^/]*/)([+-]\\d+)?")) {// 如果逗号在// ??中间，实际是单行地址
					address[0] = address[1] = singleAddress(place);
					return address;
				}
				int indexOfSlash, indexOfQuery;
				int indexOfComma = place.indexOf(',');// 储存应该用来分割的,的位置，如果找不到返回-1
				for (int i = 0; i < place.length(); i++) {
					if (place.charAt(i) == '/') {
						indexOfSlash = place.substring(i + 1).indexOf('/') + i + 1;// 找到第二个/的index，注意做了substring运算后的位置
						if (place.substring(indexOfSlash).indexOf(',') != -1) {
							// 能找到第二个/后的第一个,的话，说明这个就是要找的作为分割的逗号
							indexOfComma = place.substring(indexOfSlash).indexOf(',') + indexOfSlash;
							address[0] = singleAddress(place.substring(0, indexOfComma));
							address[1] = singleAddress(place.substring(indexOfComma + 1));
							return address;
						}
					} else if (place.charAt(i) == '?') {
						indexOfQuery = place.substring(i + 1).indexOf('?') + i + 1;// 找到第二个？的index
						if (place.substring(indexOfQuery).indexOf(',') != -1) {
							// 能找到第二个？后的第一个,的话，说明这个就是要找的作为分割的逗号
							indexOfComma = place.substring(indexOfQuery).indexOf(',') + indexOfQuery;
							address[0] = singleAddress(place.substring(0, indexOfComma));
							address[1] = singleAddress(place.substring(indexOfComma + 1));
							return address;
						}
					}
				} // 如果都找不到/ ? 的话，说明是常规的逗号，直接根据逗号位置区分两个地址
				address[0] = singleAddress(place.substring(0, indexOfComma));
				address[1] = singleAddress(place.substring(indexOfComma + 1));
			}
		}
		return address;
	}

	// 解析单行地址
	public static int singleAddress(String address) {
		String directStr = "";
		if (address.matches("[0-9]{1,}")) {
			return Integer.parseInt(address);
		} else if (address.equals("$")) {
			return Data.current.size();
		} else if (address.equals(".")) {
			return Data.currentLine;
		} else if (address.matches("/[^/]+/")) {// /str/型匹配
			directStr = address.substring(1, address.length() - 1);
			for (int i = Data.currentLine; i < Data.current.size(); i++) {// 从当前行下一行开始寻找
				if (Data.current.get(i).contains(directStr)) {
					return i + 1;
				}
			}
			for (int j = 0; j < Data.current.size(); j++) {// 若无结果继续从第一行向下寻找
				if (Data.current.get(j).contains(directStr)) {
					return j + 1;
				}
			}
		} else if (address.matches("\\?[^\\?]+\\?")) { // ?str? 型匹配
			directStr = address.substring(1, address.length() - 1);
			for (int i = Data.currentLine - 2; i >= 0; i--) {// 从当前行上一行开始寻找
				if (Data.current.get(i).contains(directStr)) {
					return i + 1;
				}
			}
			for (int j = Data.current.size() - 1; j >= 0; j--) {// 若无结果继续从最后一行向上寻找
				if (Data.current.get(j).contains(directStr)) {
					return j + 1;
				}
			}
		} else if (address.matches("'[a-z]")) {
			return Data.current.indexOf(Data.signLines.get(address.charAt(1))) + 1;// 返回标记地址
		}
		// 若上述地址格式均不符合，说明必含有+-号(不在//??内时每个只出现一次），从后面开始找到的第一个+-号即分隔符
		char firstChar = address.charAt(0);// 首字母
		int offset = 0; // 加减号后面的数字，偏移量
		if (address.lastIndexOf('+') > address.lastIndexOf('-')) { // +号在-号右边时，实际的运算符为+
			try {
				offset = Integer.parseInt(address.substring(address.lastIndexOf('+') + 1));
			} catch (NumberFormatException e) {// 如果+号后面不是数字则报错
				return -1;
			}
			if (firstChar == '+') {
				return Data.currentLine + offset;
			} else {
				return singleAddress(address.substring(0, address.lastIndexOf('+'))) + offset;
			}
		} else if (address.lastIndexOf('+') < address.lastIndexOf('-')) {// +号在-号左边时，实际的运算符为-
			try {
				offset = Integer.parseInt(address.substring(address.lastIndexOf('-') + 1));
			} catch (NumberFormatException e) {// 如果-号后面不是数字则报错
				return -1;
			}
			if (firstChar == '-') {
				return Data.currentLine - offset;
			} else {
				return singleAddress(address.substring(0, address.lastIndexOf('-'))) - offset;
			}
		}
		// 如果跳到这里，说明地址里面不含+-号，而且以上地址类型均不符合，报错
		return -1;// 表示错误地址
	}

	// 取出地址部分
	public static String queryAddress(String order) {
		// 采用正则表达式来匹配地址
		String regix = ",|;|('[a-z]|/[^/]+/|\\?[^\\?]+\\?|\\.|\\$|\\d)?([+-]\\d+)?(,('[a-z]|/[^/]+/|\\?[^//?]+\\?|\\.|\\$|\\d)?([+-]\\d+)?)?";
		Pattern rePattern = Pattern.compile(regix);
		Matcher matcher = rePattern.matcher(order);
		if (matcher.find()) {
			return matcher.group();
		}
		return "";
	}

	// 判断一般命令的地址是否合法
	public static boolean isValidAddress(int[] address) {
		if (address[0] <= address[1] && address[0] > 0 && address[1] <= Data.current.size()) {
			return true;
		}
		System.out.println("?");
		return false;
	}

	// 判断输入模式命令的地址是否合法(0a, 0i)
	public static boolean isInputValid(int[] address) {
		if (address[0] <= address[1] && address[0] >= 0 && address[1] <= Data.current.size()) {
			return true;
		}
		System.out.println("?");
		return false;
	}

}