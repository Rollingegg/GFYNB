package eDLineEditor;

public class EDLineEditor {

	/**
	 * 接收用户控制台的输入，解析命令，根据命令参数做出相应处理。 不需要任何提示输入，不要输出任何额外的内容。
	 * 输出换行时，使用System.out.println()。或者换行符使用System.getProperty("line.separator")。
	 * 待测方法为public static void main(String[] args)方法。args不传递参数，所有输入通过命令行进行。 方便手动运行。
	 * 说明：可以添加其他类和方法，但不要删除该文件，改动该方法名和参数，不要改动该文件包名和类名
	 */
	public static void main(String[] args) {
		EDLineEditor edle = new EDLineEditor();
		edle.startEDLine();
	}

	public void startEDLine() {
		CommandInterpretation ci = new CommandInterpretation();
		ci.startCommand();
	}
}