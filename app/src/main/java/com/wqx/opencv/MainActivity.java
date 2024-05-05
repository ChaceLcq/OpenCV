package com.wqx.opencv;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

/*
功能介绍：深入OpenCV Android应用开发第二章代码，检测图像的基本特征
        包括了Canny边缘检测法Sobel边缘检测法等
实现步骤：1.从手机中取出一张图片作为原始图片,通过点击menu对应的按钮开始选择图片
        2.通过menu按钮选择要对照片进行的图像处理
 */
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.SeekBar;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Random;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final static int CANNY = 0;
    private final static int HARRIS = 1;
    private final static int HOUGH = 2;
    private final static int HOUGH2 = 3;
    private final static String TAG = "infor";
    private static int Xmax = 3024;
    private static int Ymax = 4032;
    public static float RangeX1 = Xmax/2;
    public static float RangeX2 = Xmax/2;
    ArrayList<ArrayList<Double>> point = new ArrayList<ArrayList<Double>>();

    private Mat src = null;//定义一个Mat型类用于临时存放选择的图片
    private Mat image = null;//用于存放得到的图片
    private Mat des = null;//用于临时存放Mat型类的图片
    private Bitmap resultBitmap;
    private ImageView pictureView = null;//定义一个ImageView类视图用于存放选择的图片
    private SeekBar mSeekBar1;
    private SeekBar mSeekBar2;
    private TextView mTextView1;
    private TextView mTextView2;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {

            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    /*在这里执行自己的语句*/
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pictureView = (ImageView) findViewById(R.id.Picture);
        mSeekBar1=findViewById(R.id.RangeX1);
        mSeekBar2=findViewById(R.id.RangeX2);
        mTextView1=findViewById(R.id.tv_progress1);
        mTextView2=findViewById(R.id.tv_progress2);
        mSeekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override  //当滑块进度改变时，会执行该方法下的代码
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                RangeX1 = i;//设置当前的透明度
                mTextView1.setText("x1 = " +i+"/"+Xmax);
            }

            @Override  //当开始滑动滑块时，会执行该方法下的代码
            public void onStartTrackingTouch(SeekBar seekBar) {
                //resultBitmap = houghLine(image);
                //Toast.makeText(MainActivity.this,"我seekbar开始滑动了",Toast.LENGTH_SHORT).show();
            }

            @Override   //当结束滑动滑块时，会执行该方法下的代码
            public void onStopTrackingTouch(SeekBar seekBar) {
                resultBitmap = houghLine(image);
                pictureView.setImageBitmap(resultBitmap);
                Toast.makeText(MainActivity.this,"图片刷新",Toast.LENGTH_SHORT).show();
            }
        });
        mSeekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override  //当滑块进度改变时，会执行该方法下的代码
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                RangeX2 = i;//设置当前的透明度
                mTextView2.setText("x2 = " +i+"/"+Xmax);
            }

            @Override  //当开始滑动滑块时，会执行该方法下的代码
            public void onStartTrackingTouch(SeekBar seekBar) {
                //resultBitmap = houghLine(image);
                //Toast.makeText(MainActivity.this,"我seekbar开始滑动了",Toast.LENGTH_SHORT).show();
            }

            @Override   //当结束滑动滑块时，会执行该方法下的代码
            public void onStopTrackingTouch(SeekBar seekBar) {
                resultBitmap = houghLine(image);
                pictureView.setImageBitmap(resultBitmap);
                Toast.makeText(MainActivity.this,"图片刷新",Toast.LENGTH_SHORT).show();
            }
        });
            Intent pictureSelectIntent = new Intent(Intent.ACTION_PICK);
            pictureSelectIntent.setType("image/");
            startActivityForResult(pictureSelectIntent, HOUGH);
    }

    /*启动openCV*/
    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }


    }

     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.menu_main, menu);
         return true;
     }

     /*在这里选取要进行的操作*/
//     @Override
//     public boolean onOptionsItemSelected(MenuItem item) {
//         // Handle action bar item clicks here. The action bar will
//         // automatically handle clicks on the Home/Up button, so long
//         // as you specify a parent activity in AndroidManifest.xml.
//         int id = item.getItemId();
//
//         //对应Canny边缘检测的按钮
//         if (id == R.id.Canny) {
//             /*下面对通过Intent对象得到选择图片的Activity，最后返回图片的信息，得到图片*/
//             Intent pictureSelectIntent = new Intent(Intent.ACTION_PICK);//设置Action
//             pictureSelectIntent.setType("image/");//设置数据的类型
//             startActivityForResult(pictureSelectIntent, CANNY);
//             return true;
//         }
//
//         //对应Harris边缘检测的按钮
//         if (R.id.Harris == id) {
//             Intent pictureSelectIntent = new Intent(Intent.ACTION_PICK);
//             pictureSelectIntent.setType("image/");
//             startActivityForResult(pictureSelectIntent, HARRIS);
//             return true;
//         }
//         //对应Hough的直线检测按钮
//         if (R.id.Hough == id) {
//             Intent pictureSelectIntent = new Intent(Intent.ACTION_PICK);
//             pictureSelectIntent.setType("image/");
//             startActivityForResult(pictureSelectIntent, HOUGH);
//             return true;
//         }
//         //对应Hough的直线检测按钮
//         if (R.id.Hough2 == id) {
//             Intent pictureSelectIntent = new Intent(Intent.ACTION_PICK);
//             pictureSelectIntent.setType("image/");
//             startActivityForResult(pictureSelectIntent, HOUGH2);
//             return true;
//         }
//         return super.onOptionsItemSelected(item);
//     }


    /*调用StartActivityForResult后的回调函数
     * 在这个函数里面得到图片然后进行相应的处理
     * */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK == resultCode) {
            switch (requestCode) {
                case CANNY:
                    try {
                        image = getPicture(data);
                        Toast.makeText(MainActivity.this, "图片选取成功", Toast.LENGTH_SHORT).show();
                        resultBitmap = canny(image);
                        pictureView.setImageBitmap(resultBitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                case HARRIS:
                    try {
                        image = getPicture(data);//得到图片
                        Toast.makeText(MainActivity.this, "图片选取成功", Toast.LENGTH_SHORT).show();
                        resultBitmap = harris(image);//角点检测的图像处理
                        pictureView.setImageBitmap(resultBitmap);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                case HOUGH:
                    try {
                        image = getPicture(data);//得到图片
                        Toast.makeText(MainActivity.this, "图片选取成功", Toast.LENGTH_SHORT).show();
                        resultBitmap = houghLine(image);
                        pictureView.setImageBitmap(resultBitmap);
                        Log.d("lcq","pictureView = " + pictureView);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                case HOUGH2:
                    try {
                        image = getPicture(data);//得到图片
                        Toast.makeText(MainActivity.this, "图片选取成功", Toast.LENGTH_SHORT).show();
                        resultBitmap = MyHoughLine2(image);
                        pictureView.setImageBitmap(resultBitmap);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    /*得到图片*/
    public Mat getPicture(Intent data) throws FileNotFoundException {
        /*下面的代码是获得手机内的图片*/
        final Uri imageUri = data.getData();//得到图片的路径
        final InputStream imageStream = getContentResolver().openInputStream(imageUri);//得到基于路径的流文件
        final Bitmap selectImage = BitmapFactory.decodeStream(imageStream);//得到了图片的位图

        /*下面将位图转换成Mat型，可以进行图片的处理*/
        src = new Mat(selectImage.getHeight(), selectImage.getWidth(), CvType.CV_8UC4);
        Utils.bitmapToMat(selectImage, src);

        return src;
    }

    /* 下面进行图片的处理
     *
     * Canny边缘处理
     */
    public Bitmap canny(Mat src) {
        Bitmap result;
        Mat grayMat = new Mat();
        Mat cannyEdges = new Mat();
        /*将图片转换成灰度图*/
        Imgproc.cvtColor(src, grayMat, Imgproc.COLOR_BGR2GRAY);
        /*得到边缘图,这里最后两个参数控制着选择边缘的阀值上限和下限*/
        Imgproc.Canny(grayMat, cannyEdges, 50, 300);
        /*将Mat图转换成位图*/
        result = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(cannyEdges, result);
        return result;
    }

    /*Harris角点检测*/
    public Bitmap harris(Mat src) {
        Bitmap resultHarris;
        Mat grayMat = new Mat();
        Mat corners = new Mat();
        /*将图片转换成灰度图*/
        Imgproc.cvtColor(src, grayMat, Imgproc.COLOR_BGR2GRAY);
        /*找出角点*/
        Mat tempDst = new Mat();
        Imgproc.cornerHarris(grayMat, tempDst, 2, 3, 0.04);
        /*归一化Harris角点的输出*/
        Mat tempDstNorm = new Mat();
        Core.normalize(tempDst, tempDstNorm, 0, 255, Core.NORM_MINMAX);
        Core.convertScaleAbs(tempDstNorm, corners);
        /*在新的图片上绘制角点*/
        Random r = new Random();
        for (int i = 0; i < tempDstNorm.cols(); i++) {
            for (int j = 0; j < tempDstNorm.rows(); j++) {
                double[] value = tempDstNorm.get(j, i);
                if (value[0] > 250) {//决定了画出哪些角点，值越大选择画出的点就越少。如果程序跑的比较慢，就是由于值选取的太小，导致画的点过多
                    Imgproc.circle(corners, new Point(i, j), 5, new Scalar(r.nextInt(255)), 2);
                }
            }
        }
        /*将Mat图转换成位图*/
        resultHarris = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);//这一步至关重要，必须初始化Bitmap对象的大小
        Utils.matToBitmap(corners, resultHarris);
        return resultHarris;
    }

    /*Hough直线检测*/
    public Bitmap houghLine(Mat src) {
        Bitmap resultHough;
        Mat grayMat = new Mat();
        Mat cannyEdges = new Mat();
        Mat lines = new Mat();
        Mat origination = new Mat(src.size(), CvType.CV_8UC4);
        src.copyTo(origination);//拷贝
        /*通过Canny得到边缘图*/
        Imgproc.cvtColor(origination, grayMat, Imgproc.COLOR_BGR2GRAY);//灰度图片
        //Imgproc.Canny(grayMat, cannyEdges, 50, 300);
        Imgproc.Canny(grayMat, cannyEdges,100,200);
        //Mat cannyEdges = new Mat(resultHough.getHeight(),resultHough.getWidth(),CvType.CV_8UC1);
        /*获得直线图*/
        //Imgproc.HoughLinesP(cannyEdges, lines, 1, Math.PI / 180, 10, 0, 50);
        //maxLineGap 点的间隔
        //minLineLength 最小线长
        /*
        Imgproc.HoughLinesP(Mat image, Mat lines, double rho, double theta, int threshold, double minLineLength, double maxLineGap)
        参数说明：
        image：源图像
        lines：hough变换后储存检测到的线条的输出矢量
        rho：以像素为单位的距离精度
        theta：以弧度为单位的角度精度
        threshold：识别某部分为一条直线时必须达到的值
        minLineLength：最低线段的长度，默认为0
        maxLineGap：允许将同一行点与点之间连接起来的最大的距离，默认为0
        */
        Imgproc.HoughLinesP(cannyEdges, lines, 1, Math.PI / 180, 100, 0, 1000);
        Imgproc.line(origination, new Point(RangeX1,0), new Point(RangeX1,Ymax), new Scalar(0, 0, 255),20);// thickness  画线的宽度
        Imgproc.line(origination, new Point(RangeX2,0), new Point(RangeX2,Ymax), new Scalar(0, 0, 255),20);// thickness  画线的宽度
        Log.d("lcq","lines.row()1 = " + lines.rows());
        Mat houghLines = new Mat();
        houghLines.create(cannyEdges.rows(), cannyEdges.cols(), CvType.CV_8UC3);//背景色   CvType.CV_8UC4 白底，CV_8UC1 黑底，CV_8UC3 直线的颜色才起作用

        Log.d("lcq","lines.row() = " + lines.rows());
        for (int i = 0; i < lines.rows(); i++) {
            ArrayList<Double> p = new ArrayList<Double>();

            double[] points = lines.get(i,0);
            /*直线斜率*/
            double theta = (points[3]-points[1])/(points[2]-points[0]);
            for (double j : points) {
                p.add(j);
            }
            p.add(theta);
            point.add(p);
            /*延长线两端延伸距离*/
            int ExtendLength = 5000;
            double x1 = points[0];
            double y1 = points[1];
            double x2 = points[2];
            double y2 = points[3];
            /*限定检测区域*/
            boolean Area = (x1>RangeX1 && x1<RangeX2) && (x2>RangeX1 && x2<RangeX2) ||
                    (x1>RangeX2 && x1<RangeX1) && (x2>RangeX2 && x2<RangeX1);
            if(Area){
            Point pt1 = new Point(x1, y1);
            Point pt2 = new Point(x2, y2);
            double dx = pt1.x - pt2.x;
            double dy = pt1.y - pt2.y;
            double length = Math.sqrt(dx * dx + dy * dy);
            /*最短检测长度*/
            boolean RequestLength = length>100;
            Log.d("lcq","tan1 = " + (y2-y1)/(x2-x1));
            Log.d("lcq","points = " + points);
            Log.d("lcq","RangeX1 = " + RangeX1);
            Log.d("lcq","RangeX2 = " + RangeX2);
                if (RequestLength) {
                    Log.d("lcq","point = " + point);
                /*对检测点构成的直线进行两端无限延伸*/
                double ExtendLengthX1 = pt2.x + (dx / length) * ExtendLength;
                double ExtendLengthY1 = pt2.y + (dy / length) * ExtendLength;
                double ExtendLengthX2 = pt1.x - (dx / length) * ExtendLength;
                double ExtendLengthY2 = pt1.y - (dy / length) * ExtendLength;
                Log.d("lcq","ExtendLengthX1 = " + ExtendLengthX1);
                Log.d("lcq","ExtendLengthY1 = " + ExtendLengthY1);
                Log.d("lcq","ExtendLengthX2 = " + ExtendLengthX2);
                Log.d("lcq","ExtendLengthY2 = " + ExtendLengthY2);
                Log.d("lcq","tan2 = " + (ExtendLengthY2-ExtendLengthY1)/(ExtendLengthX2-ExtendLengthX1));
                Point ExtendPoint1 = new Point(ExtendLengthX1, ExtendLengthY1);
                Point ExtendPoint2 = new Point(ExtendLengthX2, ExtendLengthY2);
                /*在一幅图像上绘制直线*/
                //Imgproc.line(origination, pt1, pt2, new Scalar(0, 255, 0),3);// thickness  画线的宽度
                Imgproc.line(origination, ExtendPoint1, ExtendPoint2, new Scalar(0, 255, 0),3);// thickness  画线的宽度
                //Imgproc.line(houghLines, pt1, pt2, new Scalar(255, 255, 0), 3);// thickness  画线的宽度
                }
            }
        }
        resultHough = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(origination, resultHough);
        return resultHough;
    }

    /*Hough直线检测*/
    public Bitmap MyHoughLine2(Mat src) {
//        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
//        // Reading the Image from the file and storing it in to a Matrix object
//        String file = "F:/worksp/opencv/images/hough_input.jpg";
//        // Reading the image
//        Mat src = Imgcodecs.imread(file,0);
//        商业请保留原文链接：https://www.yiibai.com/opencv/opencv_hough_line_transform.html
        // Detecting edges of it
        Mat canny = new Mat();
        Imgproc.Canny(src, canny, 50, 200, 3, false);
        Bitmap resultHough;
        // Changing the color of the canny
        Mat cannyColor = new Mat();
        Imgproc.cvtColor(canny, cannyColor, Imgproc.COLOR_GRAY2BGR);
        // Detecting the hough lines from (canny)
        Mat lines = new Mat();
        Imgproc.HoughLines(canny, lines, 1, Math.PI / 180, 100);
        // Drawing lines on the image
        double[] data;
        double rho, theta;
        Point pt1 = new Point();
        Point pt2 = new Point();
        double a, b;
        double x0, y0;
        for (int i = 0; i < lines.cols(); i++) {
            data = lines.get(0, i);
            rho = data[0];
            theta = data[1];
            a = Math.cos(theta);
            b = Math.sin(theta);
            x0 = a * rho;
            y0 = b * rho;
            pt1.x = Math.round(x0 + 1000 * (-b));
            pt1.y = Math.round(y0 + 1000 * (a));
            pt2.x = Math.round(x0 - 1000 * (-b));
            pt2.y = Math.round(y0 - 1000 * (a));
            Imgproc.line(cannyColor, pt1, pt2, new Scalar(0, 100, 255), 6);
        }
        // Writing the image
        //Imgcodecs.imwrite("F:/worksp/opencv/images/hough_output.jpg", cannyColor);//原文出自【易百教程】，商业转载请联系作者获得授权，非商业请保留原文链接：https://www.yiibai.com/opencv/opencv_hough_line_transform.html
        resultHough = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(canny, resultHough);
        return resultHough;
    }
}