import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;

public class JumpMain {
    //按压点的坐标
    private  static  final int   swipeX     = 550;

    private static final int   swipeY     = 1580;

    public static void main(String[] args) throws InterruptedException {



        File file = new File("D:/jumpCheat");
        if (!file.exists())
             file.mkdir();

        while(true) {
            //跳一次
            jumpOneTime();
            //适当延迟，让截图不会被其他信息干扰
            Thread.sleep(1500);
        }


    }
    public static void jumpOneTime(){

        try {
        //截图后保存到本地
            executeCommand("adb shell screencap -p /sdcard/1.png");
            executeCommand("adb pull /sdcard/1.png  d:/jumpCheat/1.png");
         }catch (Exception e){
            e.printStackTrace();

        }
        //引用opencv库
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        //读取本地图片，src是彩色,matchSrc是灰度图，用来match棋子
        Mat src = Imgcodecs.imread("D:\\jumpCheat\\1.png", 1);
        Mat matchSrc = Imgcodecs.imread("D:\\jumpCheat\\1.png", 0);
        //为提高识别率，先对图片进行处理
        handlePic(src);



        //为放转换的灰度图申请空间
        Mat srcGrey = new Mat(src.rows(), src.cols(), CvType.CV_32FC1);


        //将处理好的彩色图片转换为灰度图，放到srcGrey
        Imgproc.cvtColor(src, srcGrey, Imgproc.COLOR_RGB2GRAY);



        //获得棋子右上脚坐标
        Point chess =  getChessPoint(srcGrey,matchSrc);

        //这是棋子底部中心坐标
        int x = (int) chess.x + 39;
        int y = (int) chess.y + 210;


        //用来放边缘检测后的图片
        Mat dst = srcGrey.clone();



        //通过Canny来进行边缘检测
        Imgproc.Canny(srcGrey, dst, 5, 10);

        //对比点的信息，来计算落脚点的X,Y坐标
        int length = getLength(chess, dst, x, y);
        //模拟按压跳动棋子
        doJump((int) (length * 1.36));
        //适当的延迟，让截屏不会有干扰信息

    }
    public static void handlePic(Mat src){

        //对图片进行处理
        //因为某个方块的图像识别率不是很高，所以这里进行的处理，提高识别率
        int num_rows = src.rows();
        int num_col = src.cols();
        for (int i = 0; i < num_rows; i++) {
            for (int j = 0; j < num_col; j++) {
                // 获取每个像素
                double[] clone = src.get(i, j).clone();
                double hun = clone[0]; // HSV hun
                if (clone[2] == 255 && clone[0] == 97 && clone[1] == 238    ) {
                    clone[0] = 255;
                    clone[1] = 255;
                    clone[2] = 255;
                }
                src.put(i, j, clone);
            }

        }

    }

    public static Point getChessPoint(Mat srcGrey,Mat matchSrc){
        URL resource = JumpMain.class.getResource("/");
        String s = resource + "template.jpg";
        String path = s.substring(6);
        Mat template = Imgcodecs.imread(path, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);

        int width = srcGrey.cols() - template.cols() + 1;
        int height = srcGrey.rows() - template.rows() + 1;
        Mat result = new Mat(height, width, CvType.CV_32FC1);
        //读取图像到矩阵中




        Imgproc.matchTemplate(matchSrc, template, result, Imgproc.TM_CCOEFF);
        Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());

        Core.MinMaxLocResult minMaxLocResult = Core.minMaxLoc(result);
        Point maxLoc = minMaxLocResult.maxLoc;
        System.out.println("chessPiece X Location:"+(int)(maxLoc.x+39) +"px");
        System.out.println("chessPiece Y Location:"+(int)(maxLoc.y +210)+"px");
        return maxLoc;
    }

    public static int getLength(Point chess, Mat dst,int x,int y ){

        //处理目标点位置的逻辑，主要是比对像素点
        boolean firstFind = false, jump = false;
        int maxY = 0;
        boolean notFound = true;
        for (int i = (int) (chess.y - 2); i > 354; i--) {
            if (jump) break;
            for (int j = 0; j < 1040; j++) {
                double[] doubles = dst.get(i, j);
                if (doubles[0] != 0) {
                    firstFind = true;
                    break;
                }
                if (j == 1039 && firstFind) {
                    int n=0;
                    for(n=0;n<1040;n++){
                        doubles = dst.get(i-1, n);
                        if (doubles[0] != 0) {
                            break;

                        }
                    }
                    if(n==1040){
                        maxY = i + 2;
                        notFound = false;
                        jump = true;
                    }

                }


            }

        }
        boolean isRight = false;
        if (notFound) {

            //先检查左边
            jump=false;

            for (int i = (int) chess.y; i < (int)(chess.y+80); i++) {
                if (jump) break;
                for (int j = 0; j < (int)(chess.x-10); j++) {
                    double[] doubles = dst.get(i, j);
                    if (doubles[0] != 0) {
                        firstFind = true;
//                            notFound = false;
                        jump = true;
                        isRight=false;
                        maxY = i + 2;

                        break;
                    }


                }


            }

            if (!jump){
                jump=false;
                //检查右边
                for (int i = (int) chess.y; i < 2000; i++) {
                    if (jump) break;
                    for (int j = (int) (chess.x + 80); j < 1040; j++) {
                        double[] doubles = dst.get(i, j);
                        if (doubles[0] != 0) {
                            firstFind = true;
//                            notFound = false;
                            jump = true;
                            maxY = i + 2;
                            isRight=true;

                            break;
                        }


                    }

                }
            }

        }
        int fir = 0, end = 0;
        firstFind = false;
        if (notFound) {
            if(!isRight){
                for (int i = 0; i < (chess.x -10); i++) {
                    double[] doubles = dst.get(maxY - 1, i);
                    if (doubles[0] != 0 && !firstFind) {
                        fir = i;
                        firstFind = true;
                    }
                    if (doubles[0] == 0 && firstFind) {
                        end = i;
                        break;
                    }
                }
            }else {
                for (int i = (int) (chess.x + 80); i < 1080; i++) {
                    double[] doubles = dst.get(maxY - 1, i);
                    if (doubles[0] != 0 && !firstFind) {
                        fir = i;
                        firstFind = true;
                    }
                    if (doubles[0] == 0 && firstFind) {
                        end = i;
                        break;
                    }
                }
            }

        } else {
            for (int i = 0; i < 1080; i++) {
                double[] doubles = dst.get(maxY - 1, i);
                if (doubles[0] != 0 && !firstFind) {
                    fir = i;
                    firstFind = true;
                }
                if (doubles[0] == 0 && firstFind) {
                    end = i;
                    break;
                }
            }
        }
        int xPoint = (fir + end) / 2;
        int minY = 0;
        for (int i = maxY + 20; i <= 2040; i++) {
            double[] doubles = dst.get(i, xPoint);
            if (doubles[0] != 0) {
                minY = i;
                break;
            }

        }
        int yPoint = (minY + maxY) / 2;
        System.out.println("target X Location:"+xPoint +" px");
        System.out.println("target Y Location:"+yPoint +" px");
        return (int) (Math.sqrt(Math.abs((x - xPoint)) * Math.abs((x - xPoint)) + Math.abs((y - yPoint)) * Math.abs((y - yPoint))));
    }


    private static void doJump(int distance)
    {
        System.out.println("distance: " + distance);
        int pressTime=distance;
        System.out.println("pressTime: " + pressTime);
        //执行按压操作
        String command = String.format("adb shell input swipe %s %s %s %s %s", swipeX, swipeY, swipeX, swipeY,
                pressTime);
        System.out.println(command);
        executeCommand(command);
    }


    private static void executeCommand(String command)
    {
        Process process = null;
        try
        {
            process = Runtime.getRuntime().exec(command);
            System.out.println("exec command start: " + command);
            process.waitFor();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line = bufferedReader.readLine();
            if (line != null)
            {
                System.out.println(line);
            }
            System.out.println("exec command end: " + command);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (process != null)
            {
                process.destroy();
            }
        }
    }
}
