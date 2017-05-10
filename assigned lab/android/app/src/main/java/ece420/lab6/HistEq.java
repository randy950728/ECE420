package ece420.lab6;


import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.opencsv.CSVReader;

import org.ejml.simple.SimpleMatrix;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static ece420.lab6.R.id.output;
//import java.util.List;
//import android.util.Log;



public class HistEq extends AppCompatActivity implements SurfaceHolder.Callback
{
    //Constants for each vector/avg/weight
    int avg_row = 1;
    int weight_row = 8;
    int vector_row = 8;
    int avg_col = 10304;
    int weight_col = 70;
    int vector_col = 10304;


    //Variables to hold input data
    double [][]avg = new double[avg_row][avg_col];
    double [][]vector = new double[vector_row][vector_col];
    double [][]weight = new double[weight_row][weight_col];


    //Variable for UI related stuff
    private Camera camera;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private SurfaceView surfaceView2;
    private SurfaceHolder surfaceHolder2;
    private ImageView test_image;
    private ImageView output_image;
    private TextView textHelper;
    private TextView inputtext;
    private TextView input;
    private TextView guess;
    private TextView result_text;
    private TextView counter_text;

    //Other stuff
    boolean previewing = false;
    private double total=0.0;
    private double sucess=0.0;
    private int width = 92;
    private int height = 112;
//    private int cam_width = 92;
//    private int cam_height = 112;
    private int cam_width = 640;
    private int cam_height = 480;
    private Random rand= new Random();
    Bitmap image;
    private Button buttonRS;
    private Button buttonTST;
    private Lock image_lock = new ReentrantLock();
    private int flag=0;
    //-------------------------------------------------------------------------------------------//
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hist_eq);

        // Lock down the app orientation
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        //Get the IDs of necessary items
        //---------------------------------------------//
        textHelper = (TextView) findViewById(R.id.Helper);
        inputtext  = (TextView) findViewById(R.id.input_text);
        input = (TextView) findViewById(R.id.textView);
        guess = (TextView) findViewById(R.id.textView2);
        textHelper.setTextColor(Color.WHITE);
        inputtext.setTextColor(Color.WHITE);
        guess.setTextColor(Color.WHITE);
        input.setTextColor(Color.WHITE);
        output_image = (ImageView) findViewById(output);
        test_image = (ImageView) findViewById(R.id.test_input);
        buttonRS = (Button) findViewById(R.id.reset);
        buttonTST = (Button) findViewById(R.id.test);
        getWindow().setFormat(PixelFormat.UNKNOWN);


        //Initialize everything needed
        //--------------------------------------------//
        start();

        //Setup Camera views//
        surfaceView = (SurfaceView)findViewById(R.id.camera_input);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        //Set up function call when Test button is clicked
        buttonTST.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
               test();
            }
        });

        //Set up function call when reset button is clicked
        buttonRS.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                flag=0;
                total = 0;
                sucess = 0;
                //result_text.setText("Counter Reset!");
                //result_text.setTextColor(Color.BLACK);
                //counter_text.setText("Sucess Rate:");
               // result_text.setTextColor(Color.BLACK);
            }
        });
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        // TODO Auto-generated method stub
        if(!previewing)
        {
            camera = Camera.open();
            if (camera != null)
            {
                try
                {
                    Camera.Parameters parameters = camera.getParameters();
                    parameters.setPreviewSize(cam_width,cam_height);
                    camera.setParameters(parameters);
                    camera.setDisplayOrientation(90);
                    camera.setPreviewDisplay(surfaceHolder);
                    camera.setPreviewCallback(new PreviewCallback() {
                        public void onPreviewFrame(byte[] data, Camera camera)
                        {
                            //Simply write camera input into bitmap
                            write_buffer(data);
                        }
                    });
                    camera.startPreview();
                    previewing = true;
                } catch (IOException e)
                {e.printStackTrace();}
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        if (camera != null && previewing)
        {
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
            previewing = false;
        }
    }

    //----------------------------------------------------------------------------//
    private double[][] convt_to_array(int num_row, int num_col, List input_list)
    {
        double [][] output = new double[num_row][num_col];
        for(int row=0; row<num_row; row++)
        {
            for(int col=0 ; col<num_col ; col++)
            {
                output[row][col]= Double.parseDouble(((String[])(input_list.get(row)))[col]);
            }
        }
        return output;
    }

    private void test()
    {
        int guess;
        int temp;
        double success_rate=0;
        int face = 0;
        int person = 0;

        //Open up a randomly generated face for testing
        face = rand.nextInt(10);
        person = rand.nextInt(10);
//        loadDataFromAsset(face,person);


        //Try to match the closest face
        flag=1;
        guess = face_recog();
        draw_closest(guess);
        total+=1;
        temp = guess/7;

        //Check for guess result, increment counter accordingly
//        if(temp == person)
//        {
//            sucess += 1;
//            //result_text.setText("Correct!");
//            //result_text.setTextColor(Color.GREEN);
//        }
//
//        else
//        {
//            result_text.setText("Incorrect!");
//            result_text.setTextColor(Color.RED);
//        }

        //Calculate success rate at update it unto the screen
//        success_rate = (double)((int) (sucess/total*10000))/100;
//        counter_text.setText("Success Rate: "+Double.toString(success_rate)+"% ");//+Integer.toString(guess)+" "+Integer.toString(person)+" "+Integer.toString(temp)+ " "+ Integer.toString(guess%7+1));
    }

    private void start()
    {
        //Declaring Variables for data reading
        //---------------------------------------------//
        CSVReader vector_csv;
        CSVReader avg_csv;
        CSVReader weight_csv;
        List vector_list;
        List avg_list;
        List weight_list;
        try
        {
            //Open input data
            vector_csv = new CSVReader(new InputStreamReader(getAssets().open("vector_5.txt")));
            avg_csv    = new CSVReader(new InputStreamReader(getAssets().open("avg_5.txt")));
            weight_csv = new CSVReader(new InputStreamReader(getAssets().open("weight_5.txt")));

            //Read input data into list
            vector_list = vector_csv.readAll();
            avg_list    = avg_csv.readAll();
            weight_list = weight_csv.readAll();

            //Convert input data from list into array
            avg    = convt_to_array(avg_row, avg_col, avg_list);
            weight = convt_to_array(weight_row, weight_col, weight_list);
            vector = convt_to_array(vector_row, vector_col, vector_list);

        }
        catch(IOException ex)
        {return;}
    }

    // Callback will be directed to this function
    //-------------------------------------------------------------------------------------------//
    protected void write_buffer( byte[] data)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);

        //Convert the input to RGB
//        byte []filter=  histeq(data,cam_width,cam_height);
        int[] rgbdata = yuv2rgb(data);
        int x_min = (cam_width-width)/2;
        int y_min = (cam_height-height)/2;

        //Aquire lock before modifying image
        image_lock.lock();
        try
        {
            // Create bitmap and manipulate orientation
            image = Bitmap.createBitmap(rgbdata, cam_width, cam_height, Bitmap.Config.ARGB_8888);
            image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
            image = Bitmap.createBitmap(image, y_min, x_min,width, height);
        } finally {image_lock.unlock();}
        if(flag==0)
        {
            test_image.setImageBitmap(image);
        }

        return;
    }

    // Convert YUV to RGB
    //-------------------------------------------------------------------------------------------//
    public int[] yuv2rgb(byte[] data)
    {
        final int frameSize = cam_width * cam_height;
        int[] rgb = new int[frameSize];

        for (int j = 0, yp = 0; j < cam_height; j++) {
            int uvp = frameSize + (j >> 1) * cam_width, u = 0, v = 0;
            for (int i = 0; i < cam_width; i++, yp++) {
                int y = (0xff & ((int) data[yp])) - 16;
                if (y < 0)
                    y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & data[uvp++]) - 128;
                    u = (0xff & data[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0)                  r = 0;
                else if (r > 262143)       r = 262143;
                if (g < 0)                  g = 0;
                else if (g > 262143)       g = 262143;
                if (b < 0)                  b = 0;
                else if (b > 262143)        b = 262143;

                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
        return rgb;
    }

    //-------------------------------------------------------------------------------------------//
    public void loadDataFromAsset(int face, int person)
    {
        // load image
        try
        {
            // get input stream
            InputStream ims = getAssets().open(('a'+Integer.toString(face)+'_'+ Integer.toString(person)+".png"));
            InputStream imd = getAssets().open(('a'+Integer.toString(face)+'_'+ Integer.toString(person)+".png"));

            // load image as Drawable
            Drawable d = Drawable.createFromStream(ims, null);

            // set image to ImageView
            test_image.setImageDrawable(d);

            //turn image into bitmap
            image = BitmapFactory.decodeStream(imd);
        }
        catch(IOException ex)
        {return;}
    }


    //-------------------------------------------------------------------------------------------//
    private void draw_bitmap(ImageView target_view, Bitmap input_bmp, double[][] input_array)
    {
        int raw_pix;
        int curr_pix;
        Bitmap output;
        output = Bitmap.createBitmap ( input_bmp.getWidth(), input_bmp.getHeight(),  Bitmap.Config.ARGB_8888 );
        for(int i = 0; i < input_bmp.getHeight(); i++)
        {
            for(int j=0 ; j<input_bmp.getWidth(); j++)
            {
                curr_pix = ((int)(input_array[0][i*image.getWidth()+j]))&0x00ff;
                raw_pix = 0xff000000;
                raw_pix = raw_pix | (curr_pix);
                raw_pix = raw_pix | (curr_pix)<<8;
                raw_pix = raw_pix | (curr_pix)<<16;
                output.setPixel(j, i, raw_pix);
            }
        }

        target_view.setImageBitmap(output);
    }

    //-------------------------------------------------------------------------------------------//
    private int face_recog()
    {
        double [][] input = new double[1][image.getHeight()*image.getWidth()];
        double [][]norm_array = new double[1][10304];
        double [] distance = new double[70];
        double [][]final_array = new double[8][70];
        double [][] project_array = new double[8][1];   //8*1 matrix
        double minimum = 1000000000;
        int min_idx=0;
        double temp=0;
        int raw_pix;
        int R,G,B,A;

        SimpleMatrix vector_matrix;
        SimpleMatrix normal_matrix;
        SimpleMatrix project_matrix;

        image_lock.lock();
        try
        {
            //Convert Input bitmap into array
            for(int i = 0; i < image.getHeight(); i++)
            {
                for(int j=0 ; j<image.getWidth(); j++)
                {
                    raw_pix = image.getPixel(j,i);
                    A = (raw_pix >> 24) &0xff;
                    R = (raw_pix >>16) &0xff;
                    G = (raw_pix >> 8) &0xff;
                    B = (raw_pix     ) &0xff;
                    input[0][i*image.getWidth()+j] = 0.2126 * R + 0.7152 * G + 0.0722 * B;
                }
            }
//            test_image.setImageBitmap(image);
            test_image.setImageBitmap(image);
        } finally {image_lock.unlock();}


        //Calculate normal face
        for(int i = 0; i < 10304; i++)
        {
            norm_array[0][i] = input[0][i] - avg[0][i];
        }

        //Move array into matrix for external library
        normal_matrix = new SimpleMatrix(norm_array);   //1*10304
        vector_matrix = new SimpleMatrix(vector);       //8*10304

        //take dot product here and set equal to project[][]
        normal_matrix = normal_matrix.transpose();
        project_matrix = vector_matrix.mult(normal_matrix);    //8*1

        //Convert matrix back to array
        for(int i =0 ; i<8 ; i++)
        {
            project_array[i][0] = project_matrix.get(i,0);
        }

        //Calculate distance from each face
        for(int i=0 ; i<70 ; i++)
        {
            for(int j=0 ; j<8 ; j++)
            {
                final_array[j][i] = weight[j][i]-project_array[j][0];
            }
        }

        //Nromalize distance
        for(int i =0 ; i<70 ; i++)
        {
            temp=0;
            for (int j=0 ; j<8 ; j++)
            {
                temp+= final_array[j][i]*final_array[j][i];
            }
            distance[i] = Math.pow(temp,0.5);
        }

        //Find closest match
        for(int i =0; i<70; i++)
        {
            if(distance[i] < minimum)
            {
                min_idx = i;
                minimum = distance[i];
            }
        }
        Log.d("debug","min idx"+Integer.toString(min_idx));
        return min_idx;
    }

    //------------------------------------------------------------------------------------------//
    private void draw_closest(int guess)
    {
        int face_num=0;
        int person=0;
        // load image
        try
        {
            person = guess/7;
            face_num = (guess%7)+1;
            // get input stream
            InputStream ims = getAssets().open(('a'+Integer.toString(face_num)+'_'+ Integer.toString(person)+".png"));

            // load image as Drawable
            Drawable d = Drawable.createFromStream(ims, null);

            // set image to ImageView
            output_image.setImageDrawable(d);
        }
        catch(IOException ex)
        {return;}
    }


    //-------------------------------------------------------------------------------------------//
    private float[][] eigen_face(float face_data[][])
    {
        int num_face = 70;
        int size = 10304;
        float avg_array[] = new float[size];
        float mean_array[][] = new float[num_face][size];
        float trans[][] = new float[size][num_face];
        float co_var[][] = new float[num_face][num_face];
        float value[] = new float [70];
        float vector[][] = new float[70][70];
        float vector_transpose[][] = new float[70][70];
        float vector_dot[][] = new float[10304][70];
        float largest_vector[][] = new float[10304][2];
        float largest_vector_transpose[][] = new float [2][10304];
        int mag=10;

        for(int pic = 0; pic < num_face; pic++)
        {
            for (int i = 0; i < size; i++)
            {
                avg_array[i] = avg_array[i]+ face_data[pic][i];
            }
        }
        for(int i = 0; i<avg_array.length; i++)
        {
            avg_array[i] = avg_array[i]/num_face;
        }

        int count = 0;
        for(int pic = 0; pic < num_face; pic++)
        {
            for(int j = 0; j <size; j++)
            {
                mean_array[count][j] = face_data[pic][j] - avg_array[j];
            }
            count = count+1;
        }

        //transpose mean_array here and set equal to trans
        //take dot product of trans and mean_array and set equal to co_var here
        for(int i =0; i<num_face; i++)
        {
            for(int j =0; j <num_face; j++)
            {
                co_var[i][j] = co_var[i][j]/num_face;
            }
        }

        //perform eigenvalue and eigenvector function call here, set equal to value and vector
        //sort the indexes and set equal to sort_index
        //take transpose of vector, set equal to vector_transpose
        //take dot product of mean_array and vector, set equal to vector_dot
        //find the normal of vector_dot, set equal to mag
        for(int i =0; i < 10304; i++)
        {
            for(int j =0; j< 70; j++)
            {
                vector_dot[i][j] = vector_dot[i][j]/mag;
            }
        }
        return largest_vector;
    }

    //-----------------------------------------------------------------------//
    // Implement this function
    public byte[] histeq(byte[] data, int width, int height)
    {
        // Your data should be stored inside here
        byte[] histeqdata = new byte[data.length];
        int[] hist = new int[256];
        int[] sum = new int[256];
        int size = height*width;
        int minimum  = 300;
        int maximum  = 0;
        int curr_data= 0;
        // Perform Histogram Equalization Here

        //Initialize Hist to 0
        //-------------------------------------------------------------//
        for(int i=0; i <hist.length; i++)
        {
            hist[i] = 0;
        }


        //Build Histogram
        //-------------------------------------------------------------//
        for(int i=0; i<size; i++)
        {
            hist[ ((int)data[i]) & (0x00FF)] +=1;
        }


        //Start looking for max and min of pixel intensity
        //-------------------------------------------------------------//
        sum[0]    = hist[0];
        minimum   = 300;
        maximum   = 0;
        curr_data = 0;
        for(int i=0; i <size; i++)
        {
            curr_data = ((int)data[i]) & 0x00FF;
            if(curr_data < minimum)
                minimum = curr_data;

            if (curr_data > maximum)
                maximum = curr_data;
        }


        //Integrating histogram data
        //-------------------------------------------------------------//
        for(int i=1; i<255; i++)
        {
            sum[i] =  (sum[i-1] + hist[i]);
        }


        //Calculate Histogram equalization array
        //-------------------------------------------------------------//
        for(int i = 0; i<255; i++)
        {
            sum[i] = (int) (((double)(sum[i] - minimum) / (double)(size - 1)) * (double)(255));
        }


        // Apply equalization to all pixels
        //-------------------------------------------------------------//
        for(int i=0; i<size; i++)
        {
            histeqdata[i] = (byte)sum[ ((int)data[i])&0x00FF ];
        }


        // Don't modify this Part, copying U,V channel data.
        //-------------------------------------------------------------//
        for(int i=size; i<data.length; i++)
        {
            histeqdata[i] = data[i];
        }


        return histeqdata;
    }

}


