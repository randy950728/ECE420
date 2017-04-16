package ece420.lab6;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Random;

import static ece420.lab6.MainActivity.appflag;
import static ece420.lab6.R.id.output;
//import java.util.List;
//import android.util.Log;



public class HistEq extends AppCompatActivity
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


    //Other variables
    int num_face = 10;
    int train_size = 7;
    int test_size = 3;
    double out;
    float new_train_face[][] = new float[num_face*train_size][10304];


    //Variable for UI related stuff
    private Camera camera;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private SurfaceView surfaceView2;
    private SurfaceHolder surfaceHolder2;
    private ImageView input_image;
    private ImageView output_image;
    private TextView textHelper;
    private TextView result_text;
    private TextView counter_text;

    //Other stuff
    private double total=0.0;
    private double sucess=0.0;
    private int width = 92;
    private int height = 112;
    private Random rand= new Random();
    Bitmap image;
    String TAG = "debug";
    private Button buttonRS;
    private Button buttonTST;
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
        result_text= (TextView) findViewById(R.id.result_display);
        counter_text=(TextView) findViewById(R.id.counter);
        input_image = (ImageView) findViewById(R.id.input);
        output_image = (ImageView) findViewById(output);
        buttonRS = (Button) findViewById(R.id.reset);
        buttonTST = (Button) findViewById(R.id.test);
        getWindow().setFormat(PixelFormat.UNKNOWN);


        //Initialize everything needed
        //--------------------------------------------//
        start();

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
                total = 0;
                sucess = 0;
                result_text.setText("Counter Reset!");
                result_text.setTextColor(Color.BLACK);
                counter_text.setText("Sucess Rate:");
                result_text.setTextColor(Color.BLACK);
            }
        });
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
        loadDataFromAsset(face,person);

        //Try to match the closest face
        guess = face_recog();
        draw_closest(guess);
        total+=1;
        temp = guess/7;
        if(temp == person)
        {
            sucess += 1;
            result_text.setText("Correct!");
            result_text.setTextColor(Color.GREEN);
        }

        else
        {
            result_text.setText("Incorrect!");
            result_text.setTextColor(Color.RED);
        }

        success_rate = (double)((int) (sucess/total*10000))/100;
        counter_text.setText("Success Rate: "+Double.toString(success_rate)+"% "+Integer.toString(guess)+" "+Integer.toString(person)+" "+Integer.toString(temp)+ " "+ Integer.toString(guess%7+1));
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
            vector_csv = new CSVReader(new InputStreamReader(getAssets().open("vector_2.txt")));
            avg_csv    = new CSVReader(new InputStreamReader(getAssets().open("avg_2.txt")));
            weight_csv = new CSVReader(new InputStreamReader(getAssets().open("weight_2.txt")));

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
    protected void drawSomething(Canvas canvas, byte[] data)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);

        if(appflag==1)
        {
            // Your function is called here
//            byte[] histeqdata = histeq(data,width,height);
            // We convert YUV to RGB For you
//            int[] rgbdata = yuv2rgb(histeqdata);

            // Create bitmap and manipulate orientation
//            Bitmap bmp = Bitmap.createBitmap(rgbdata,width,height, ARGB_8888);
//            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

            // Draw the bitmap
//            canvas.drawBitmap(bmp,new Rect(0,0,height,width),new Rect(0,0,canvas.getWidth(),canvas.getHeight()),null);
            return;
        }
    }

    // Convert YUV to RGB
    //-------------------------------------------------------------------------------------------//
    public int[] yuv2rgb(byte[] data)
    {
        final int frameSize = width * height;
        int[] rgb = new int[frameSize];

        for (int j = 0, yp = 0; j < height; j++)
        {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
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


    // Converting to Grayscale and Merge
    //-------------------------------------------------------------------------------------------//
    public int[] merge(int[] xdata,int[] ydata)
    {
        int size = height*width;
        int[] edgedata = new int[size];
        for(int i=0;i<size;i++)
        {
            int p = (int)Math.sqrt((double)((xdata[i]&0xff)*(xdata[i]&0xff) + (ydata[i]&0xff)*(ydata[i]&0xff))/2);
            edgedata[i] = 0xff000000 | p<<16 | p<<8 | p;
        }
        return edgedata;
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
            input_image.setImageDrawable(d);

            //turn image into bitmap
            image = BitmapFactory.decodeStream(imd);
        }
        catch(IOException ex)
        {return;}
    }


    //-------------------------------------------------------------------------------------------//
    private void draw_bitmag(ImageView target_view, Bitmap input_bmp, double[][] input_array)
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


    //-------------------------------------------------------------------------------------------//
    private float[] convt(float face[][])
    {
        int height = 92;
        int width = 112;
        float new_face[] = new float[10304];
        for(int y = 0; y<height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                new_face[x + y * width] = face[y][x];
            }
        }
        return new_face;
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

}