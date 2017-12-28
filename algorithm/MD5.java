//传入参数:一个字节数组
//传出参数:字节数组的MD5结果字符串
public class MD5{
    public static String getMD5(byte[] source){
        String s=null;
        //用来将字节转换成十六进制的字符
        char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};

        try
        {
            //java.security.MessageDigest中已经定义了MD5计算
            java.security.MessageDigest md=
            java.security.MessageDigest.getInstance("MD5");
            md.updata(source);
            byte tmp[] = md.digest();
            //MD5的计算结果是一个128位的长整数
            char str[] = new char[16*2];
            //每个字符用十六进制表示，使用两个字符，所以表示成十六进制需要32个字符
            int k=0;   //表示转换结果中对应的字符位置
            for(int i=0;i<16;i++){     
            //从第一个字节开始将MD5每一个字节转换成十六进制字符
                byte byte0 = tmp[i];   
                //取字节中高四位的数字转换,将符号位一起右移动
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf]; //取字节中低地位的字节转换
            }
            //转换后的结果转换为字符串
            s = new String(str);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return s;
    }
}