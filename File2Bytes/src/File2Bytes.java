import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

//by radioegor146
public class File2Bytes {
    public static void run(String in, String out) throws Exception {
        OutputStream outputStream = new FileOutputStream(out);
        String string = byteArrayToString(new FileInputStream(in).readAllBytes());
        outputStream.write(string.getBytes(StandardCharsets.UTF_8));
    }

    public static void main(String[] args) throws Exception {
        //这个程序的功能就是把一个文件转为文本（为了将文件写进源码），没别的作用
        //同理下面的路径，前面是输入，后面是输出
        run(args[0], args[1]);
    }

    public static String byteArrayToString(byte[] data) {
        StringBuilder result = new StringBuilder();
        result.append("{");
        for (byte b : data) {
            result.append(b).append(",");
        }
        result.append("}");
        return result.toString();
    }
}