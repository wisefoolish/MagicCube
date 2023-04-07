package com.example.rubikcube;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

public class RubikCube {
    static class Vec3
    {
        public double xx,yy,zz;
        public Vec3()
        {
            this.xx=0;
            this.yy=0;
            this.zz=0;
        }
        public Vec3(double xx,double yy,double zz)
        {
            this.xx=xx;
            this.yy=yy;
            this.zz=zz;
        }
        // 得到向量模长
        public double GetMouldLength()
        {
            return Math.sqrt(xx*xx+yy*yy+zz*zz);
        }
        // 向量加法
        public static Vec3 AddOperation(Vec3 a,Vec3 b)
        {
            return new Vec3(a.xx+b.xx,a.yy+b.yy,a.zz+b.zz);
        }
        // 向量减法
        public static Vec3 Subtraction(Vec3 a,Vec3 b)
        {
            return new Vec3(a.xx-b.xx,a.yy-b.yy,a.zz-b.zz);
        }
        // 向量点乘
        public static double MultiplyOperation(Vec3 a,Vec3 b)
        {
            return a.xx*b.xx+a.yy*b.yy+a.zz*b.zz;
        }
        // 向量乘法，实现缩放
        public static Vec3 MultiplyOperation(Vec3 a,double b)
        {
            return new Vec3(a.xx*b,a.yy*b,a.zz*b);
        }
        // 向量除法，实现缩放
        public static Vec3 DivisionOperation(Vec3 a,double b)
        {
            // if(b==0.0)return new Vec3();
            return new Vec3(a.xx/b,a.yy/b,a.zz/b);
        }
        // 得到两个向量之间的余弦值
        public static double GetCosBetweenVector(Vec3 a,Vec3 b)
        {
            // a*b=|a|*|b|*cos(θ)
            // cos(θ)=a*b/(|a|*|b|)
            return MultiplyOperation(a,b)/(b.GetMouldLength()*a.GetMouldLength());
        }
        // 得到这个向量对另一个向量的投影
        public Vec3 GetProject(Vec3 ano)
        {
            double cosTh=GetCosBetweenVector(this,ano);
            return MultiplyOperation(ano,this.GetMouldLength()*cosTh/ano.GetMouldLength());
        }
        // 得到投影模长
        //public double GetProjectMouldLength(Vec3 ano)
        //{
        //    return this.GetMouldLength()*GetCosBetweenVector(this,ano);
        //}
        // 向量叉乘，满足右手坐标系，参数填反就满足左手坐标系
        public static Vec3 MultiplicationCross(Vec3 X_Vector,Vec3 Y_Vector) {
            // (y0z1 - y1z0, x1z0 - x0z1, x0y1 - x1y0)
            Vec3 result= new Vec3(X_Vector.yy * Y_Vector.zz - Y_Vector.yy * X_Vector.zz,
                    Y_Vector.xx * X_Vector.zz - X_Vector.xx * Y_Vector.zz,
                    X_Vector.xx * Y_Vector.yy - Y_Vector.xx * X_Vector.yy);
            result=Vec3.DivisionOperation(result,result.GetMouldLength());
            return result;
        }
    }
    static class Vec2
    {
        public double xx,yy;
        public Vec2()
        {
            this.xx=0;
            this.yy=0;
        }
        public Vec2(double xx,double yy)
        {
            this.xx=xx;
            this.yy=yy;
        }
        // 得到向量模长
        public double GetMouldLength()
        {
            return Math.sqrt(xx*xx+yy*yy);
        }
        // 向量加法
        public static Vec2 AddOperation(Vec2 a,Vec2 b)
        {
            return new Vec2(a.xx+b.xx,a.yy+b.yy);
        }
        // 向量减法
        public static Vec2 Subtraction(Vec2 a,Vec2 b)
        {
            return new Vec2(a.xx-b.xx,a.yy-b.yy);
        }
        // 相量点乘
        public static double MultiplyOperation(Vec2 a,Vec2 b)
        {
            return a.xx*b.xx+a.yy*b.yy;
        }
        // 向量乘法，实现缩放
        public static Vec2 MultiplyOperation(Vec2 a,double b)
        {
            return new Vec2(a.xx*b,a.yy*b);
        }
        // 向量除法，实现缩放
        public static Vec2 DivisionOperation(Vec2 a,double b)
        {
            // if(b==0.0)return new Vec3();
            return new Vec2(a.xx/b,a.yy/b);
        }
        // 得到两个向量之间的余弦值
        public static double GetCosBetweenVector(Vec2 a,Vec2 b)
        {
            // a*b=|a|*|b|*cos(θ)
            // cos(θ)=a*b/(|a|*|b|)
            return MultiplyOperation(a,b)/(b.GetMouldLength()*a.GetMouldLength());
        }
        // 得到这个向量对另一个向量的投影
        public Vec2 GetProject(Vec2 ano)
        {
            double cosTh=GetCosBetweenVector(this,ano);
            return MultiplyOperation(ano,this.GetMouldLength()*cosTh/ano.GetMouldLength());
        }
    }

    class CubeIndex
    {
        int plane;
        int index;
    }

    class RotationPoint
    {
        int RotationiAxis;
        int RotateJudge;
    }

    // 右前上
    // 0右1左2上3下4前5后
    // 红右，绿左，蓝上，橙下，白前，黄后
    int[] AllColor={0xffff0000,0xff00ff00,0xff0000ff,0xffff8000,0xffffffff,0xffffff00};
    public int[][] SurfaceColor=new int[6][9];
    void InitSurfaceColor()
    {
        for(int i=0;i<6;i++)
        {
            for(int j=0;j<9;j++)
            {
                SurfaceColor[i][j]=AllColor[i];
            }
        }
    }


    double sidelength_rc;
    private double imagingtimes=5.0;
    Vec3[] Vertex_Rubik=new Vec3[8];
    void InitVertex()
    {
        double SideLength=sidelength_rc;
        Vertex_Rubik[0]=new Vec3(-SideLength/2.0,-SideLength/2.0,-SideLength/2.0);
        Vertex_Rubik[1]=new Vec3(SideLength/2.0,-SideLength/2.0,-SideLength/2.0);
        Vertex_Rubik[2]=new Vec3(SideLength/2.0,SideLength/2.0,-SideLength/2.0);
        Vertex_Rubik[3]=new Vec3(-SideLength/2.0,SideLength/2.0,-SideLength/2.0);
        Vertex_Rubik[4]=new Vec3(-SideLength/2.0,-SideLength/2.0,SideLength/2.0);
        Vertex_Rubik[5]=new Vec3(SideLength/2.0,-SideLength/2.0,SideLength/2.0);
        Vertex_Rubik[6]=new Vec3(SideLength/2.0,SideLength/2.0,SideLength/2.0);
        Vertex_Rubik[7]=new Vec3(-SideLength/2.0,SideLength/2.0,SideLength/2.0);
    }

    public Vec3 X_Vector,Y_Vector;

    boolean isEntirety;
    double rotateAngle;

    CubeIndex mCubeIndex;
    RotationPoint mRotationPoint;

    public RubikCube(double SideLength)
    {
        // 初始化颜色数组
        InitSurfaceColor();
        X_Vector=new Vec3(1/Math.sqrt(2),1/Math.sqrt(2),0);
        Y_Vector=new Vec3(-1/Math.sqrt(3),1/Math.sqrt(3),1/Math.sqrt(3));
        // 初始化魔方顶点
        sidelength_rc=SideLength;
        InitVertex();
        isEntirety=true;
        rotateAngle=0;
        mCubeIndex=new CubeIndex();
        mCubeIndex.plane=-1;
        mCubeIndex.index=-1;
        mRotationPoint=new RotationPoint();
        mRotationPoint.RotationiAxis=-1;
        mRotationPoint.RotateJudge=-1;
    }

    // 整体旋转
    void RotateEntirety(double Alpha,boolean isHorizontal)
    {
        double cos_A=Math.cos(Alpha);
        double sin_A=Math.sin(Alpha);
        Vec3 Z_Vector=Vec3.MultiplicationCross(X_Vector,Y_Vector);
        if(isHorizontal)
        {
            X_Vector=Vec3.AddOperation(Vec3.MultiplyOperation(X_Vector,cos_A),Vec3.MultiplyOperation(Z_Vector,sin_A));
            X_Vector=Vec3.DivisionOperation(X_Vector,X_Vector.GetMouldLength());
        }
        else
        {
            Y_Vector=Vec3.AddOperation(Vec3.MultiplyOperation(Y_Vector,cos_A),Vec3.MultiplyOperation(Z_Vector,sin_A));
            Y_Vector=Vec3.DivisionOperation(Y_Vector,Y_Vector.GetMouldLength());
        }
    }
    void RotateEntirety(double Th,double Fi)
    {
        RotateEntirety(Th,true);
        RotateEntirety(Fi,false);
    }

    // 将三维的点根据投影面转化为二维的点
    Vec2 Transform3DTo2D(Vec3 Vertex)
    {
        double cos_X=Vec3.GetCosBetweenVector(Vertex,X_Vector);
        double cos_Y=Vec3.GetCosBetweenVector(Vertex,Y_Vector);
        double length=Vertex.GetMouldLength();
        Vec2 result= new Vec2(length*cos_X,length*cos_Y);
        // return result;
        // 以上是平行投影的效果
        Vec3 vertical=Vec3.MultiplyOperation(Vec3.MultiplicationCross(X_Vector,Y_Vector),sidelength_rc*imagingtimes);
        // 标志
        Vec3 target=Vec3.Subtraction(Vertex,vertical);
        result=Vec2.MultiplyOperation(result,vertical.GetMouldLength()/target.GetProject(vertical).GetMouldLength());
        return result;
    }

    void Line(Vec2 begin, Vec2 end, Canvas canvas, Paint paint)
    {
        canvas.drawLine((float)begin.xx,(float)begin.yy,(float)end.xx,(float)end.yy,paint);
    }

    // 填充矩形
    void PackedQuadrilateral(Vec2 p1,Vec2 p2,Vec2 p3,Vec2 p4,Canvas canvas,int fillcolor)
    {
        Paint rectangleColor=new Paint();
        rectangleColor.setStyle(Paint.Style.FILL_AND_STROKE);
        rectangleColor.setColor(fillcolor);
        rectangleColor.setAntiAlias(true);
        Path path=new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo((float) p1.xx,(float) p1.yy);
        path.lineTo((float) p2.xx,(float) p2.yy);
        path.lineTo((float) p3.xx,(float) p3.yy);
        path.lineTo((float) p4.xx,(float) p4.yy);
        path.close();

        canvas.drawPath(path,rectangleColor);
    }

    void DrawSurface(Vec3[] Surface,int[] ColorArray,Canvas canvas)
    {
        Vec2 pericenter_Sur=new Vec2(canvas.getWidth()*0.5,canvas.getHeight()*0.5);

        Vec3 add_X = Vec3.DivisionOperation(Vec3.Subtraction(Surface[1],Surface[0]),3);
        Vec3 add_Y = Vec3.DivisionOperation(Vec3.Subtraction(Surface[3],Surface[0]),3);
        Vec3 add_All = Vec3.AddOperation(add_X,add_Y);
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                // 当前起点
                Vec3 position = Vec3.AddOperation(Vec3.AddOperation(Surface[0],Vec3.MultiplyOperation(add_X,j)),Vec3.MultiplyOperation(add_Y,i));
                PackedQuadrilateral(Vec2.AddOperation(Transform3DTo2D(position),pericenter_Sur),
                        Vec2.AddOperation(Transform3DTo2D(Vec3.AddOperation(position,add_X)),pericenter_Sur),
                        Vec2.AddOperation(Transform3DTo2D(Vec3.AddOperation(position,add_All)),pericenter_Sur),
                        Vec2.AddOperation(Transform3DTo2D(Vec3.AddOperation(position,add_Y)),pericenter_Sur),canvas,ColorArray[i * 3 + j]);

                // 外圈图一层黑色
                Paint Linecolor=new Paint();
                Linecolor.setAntiAlias(true);
                Linecolor.setColor(0xff000000);
                Line(Vec2.AddOperation(Transform3DTo2D(position),pericenter_Sur),
                        Vec2.AddOperation(Transform3DTo2D(Vec3.AddOperation(position,add_X)),pericenter_Sur),canvas,Linecolor);
                Line(Vec2.AddOperation(Transform3DTo2D(position),pericenter_Sur),
                        Vec2.AddOperation(Transform3DTo2D(Vec3.AddOperation(position,add_Y)),pericenter_Sur),canvas,Linecolor);
                Line(Vec2.AddOperation(Transform3DTo2D(Vec3.AddOperation(position,add_All)),pericenter_Sur),
                        Vec2.AddOperation(Transform3DTo2D(Vec3.AddOperation(position,add_X)),pericenter_Sur),canvas,Linecolor);
                Line(Vec2.AddOperation(Transform3DTo2D(Vec3.AddOperation(position,add_All)),pericenter_Sur),
                        Vec2.AddOperation(Transform3DTo2D(Vec3.AddOperation(position,add_Y)),pericenter_Sur),canvas,Linecolor);
            }
        }
    }

    // 画方块，也即未旋转的魔方
    void DrawCube(Canvas canvas)
    {
        Vec3 Z_Vector=Vec3.MultiplicationCross(X_Vector,Y_Vector);

        Vec3[] temp_Vertex=new Vec3[4];

        // 0右1左2上3下4前5后
        // 判断右面能不能贴出
        if(Z_Vector.xx>1/(imagingtimes*2))
        {
            temp_Vertex[0]=Vertex_Rubik[1];
            temp_Vertex[1]=Vertex_Rubik[2];
            temp_Vertex[2]=Vertex_Rubik[6];
            temp_Vertex[3]=Vertex_Rubik[5];
            DrawSurface(temp_Vertex,SurfaceColor[0],canvas);
        }
        // 判断左面能不能贴出
        else if(Z_Vector.xx<-1/(imagingtimes*2))
        {
            temp_Vertex[0]=Vertex_Rubik[0];
            temp_Vertex[1]=Vertex_Rubik[3];
            temp_Vertex[2]=Vertex_Rubik[7];
            temp_Vertex[3]=Vertex_Rubik[4];
            DrawSurface(temp_Vertex,SurfaceColor[1],canvas);
        }

        // 判断上面能不能贴出
        if(Z_Vector.zz>1/(imagingtimes*2))
        {
            temp_Vertex[0]=Vertex_Rubik[4];
            temp_Vertex[1]=Vertex_Rubik[5];
            temp_Vertex[2]=Vertex_Rubik[6];
            temp_Vertex[3]=Vertex_Rubik[7];
            DrawSurface(temp_Vertex,SurfaceColor[2],canvas);
        }
        // 判断下面能不能贴出
        else if(Z_Vector.zz<-1/(imagingtimes*2))
        {
            temp_Vertex[0]=Vertex_Rubik[0];
            temp_Vertex[1]=Vertex_Rubik[1];
            temp_Vertex[2]=Vertex_Rubik[2];
            temp_Vertex[3]=Vertex_Rubik[3];
            DrawSurface(temp_Vertex,SurfaceColor[3],canvas);
        }

        // 判断前面能不能贴出
        if(Z_Vector.yy<-1/(imagingtimes*2))
        {
            temp_Vertex[0]=Vertex_Rubik[0];
            temp_Vertex[1]=Vertex_Rubik[1];
            temp_Vertex[2]=Vertex_Rubik[5];
            temp_Vertex[3]=Vertex_Rubik[4];
            DrawSurface(temp_Vertex,SurfaceColor[4],canvas);
        }
        // 判断后面能不能贴出
        else if(Z_Vector.yy>1/(imagingtimes*2))
        {
            temp_Vertex[0]=Vertex_Rubik[3];
            temp_Vertex[1]=Vertex_Rubik[2];
            temp_Vertex[2]=Vertex_Rubik[6];
            temp_Vertex[3]=Vertex_Rubik[7];
            DrawSurface(temp_Vertex,SurfaceColor[5],canvas);
        }
    }

    // 横向的一条
    void DrawBar_Across(Vec3[] Surface,int[] ColorArray,Canvas canvas)
    {
        Vec2 pericenter_Sur=new Vec2(canvas.getWidth()/2,canvas.getHeight()/2);
        Vec3 add_X = Vec3.DivisionOperation(Vec3.Subtraction(Surface[1],Surface[0]),3);
        Vec3 add_Y = Vec3.Subtraction(Surface[3],Surface[0]);
        Vec3 add_All = Vec3.AddOperation(add_X,add_Y);
        for(int i=0;i<3;i++)
        {
            Vec3 position = Vec3.AddOperation(Surface[0],Vec3.MultiplyOperation(add_X,i));
            PackedQuadrilateral(Vec2.AddOperation(Transform3DTo2D(position),pericenter_Sur),
                    Vec2.AddOperation(Transform3DTo2D(Vec3.AddOperation(position,add_X)),pericenter_Sur),
                    Vec2.AddOperation(Transform3DTo2D(Vec3.AddOperation(position,add_All)),pericenter_Sur),
                    Vec2.AddOperation(Transform3DTo2D(Vec3.AddOperation(position,add_Y)),pericenter_Sur),canvas,ColorArray[i]);

            // 外圈图一层黑色
            Paint Linecolor=new Paint();
            Linecolor.setAntiAlias(true);
            Linecolor.setColor(0xff000000);
            Line(Vec2.AddOperation(Transform3DTo2D(position),pericenter_Sur),
                    Vec2.AddOperation(Transform3DTo2D(Vec3.AddOperation(position,add_X)),pericenter_Sur),canvas,Linecolor);
            Line(Vec2.AddOperation(Transform3DTo2D(position),pericenter_Sur),
                    Vec2.AddOperation(Transform3DTo2D(Vec3.AddOperation(position,add_Y)),pericenter_Sur),canvas,Linecolor);
            Line(Vec2.AddOperation(Transform3DTo2D(Vec3.AddOperation(position,add_All)),pericenter_Sur),
                    Vec2.AddOperation(Transform3DTo2D(Vec3.AddOperation(position,add_X)),pericenter_Sur),canvas,Linecolor);
            Line(Vec2.AddOperation(Transform3DTo2D(Vec3.AddOperation(position,add_All)),pericenter_Sur),
                    Vec2.AddOperation(Transform3DTo2D(Vec3.AddOperation(position,add_Y)),pericenter_Sur),canvas,Linecolor);
        }
    }
    // 竖向的一条
    void DrawBar_Straight(Vec3[] Surface,int[] ColorArray,Canvas canvas)
    {
        Vec2 pericenter_Sur=new Vec2(canvas.getWidth()/2,canvas.getHeight()/2);
        Vec3 add_X = Vec3.Subtraction(Surface[1],Surface[0]);
        Vec3 add_Y = Vec3.DivisionOperation(Vec3.Subtraction(Surface[3],Surface[0]),3);
        Vec3 add_All = Vec3.AddOperation(add_X,add_Y);
        for(int i=0;i<3;i++)
        {
            Vec3 position = Vec3.AddOperation(Surface[0],Vec3.MultiplyOperation(add_Y,i));
            PackedQuadrilateral(Vec2.AddOperation(Transform3DTo2D(position),pericenter_Sur),
                    Vec2.AddOperation(Transform3DTo2D(Vec3.AddOperation(position,add_X)),pericenter_Sur),
                    Vec2.AddOperation(Transform3DTo2D(Vec3.AddOperation(position,add_All)),pericenter_Sur),
                    Vec2.AddOperation(Transform3DTo2D(Vec3.AddOperation(position,add_Y)),pericenter_Sur),canvas,ColorArray[i]);

            // 外圈图一层黑色
            Paint Linecolor=new Paint();
            Linecolor.setAntiAlias(true);
            Linecolor.setColor(0xff000000);
            Line(Vec2.AddOperation(Transform3DTo2D(position),pericenter_Sur),
                    Vec2.AddOperation(Transform3DTo2D(Vec3.AddOperation(position,add_X)),pericenter_Sur),canvas,Linecolor);
            Line(Vec2.AddOperation(Transform3DTo2D(position),pericenter_Sur),
                    Vec2.AddOperation(Transform3DTo2D(Vec3.AddOperation(position,add_Y)),pericenter_Sur),canvas,Linecolor);
            Line(Vec2.AddOperation(Transform3DTo2D(Vec3.AddOperation(position,add_All)),pericenter_Sur),
                    Vec2.AddOperation(Transform3DTo2D(Vec3.AddOperation(position,add_X)),pericenter_Sur),canvas,Linecolor);
            Line(Vec2.AddOperation(Transform3DTo2D(Vec3.AddOperation(position,add_All)),pericenter_Sur),
                    Vec2.AddOperation(Transform3DTo2D(Vec3.AddOperation(position,add_Y)),pericenter_Sur),canvas,Linecolor);
        }
    }

    void DrawLayer_Across(Vec3[] Temp_Vertex,int[][] BarColorArray,int[][] nineColor,Canvas canvas)
    {
        Vec3 Z_Vector=Vec3.MultiplicationCross(X_Vector,Y_Vector);
        // 我可真是个小天才，这几步太秀了
        // 其实完全没必要啊，这几步好多余
        Vec3 temp_1 = Vec3.Subtraction(Temp_Vertex[7],Temp_Vertex[0]);
        Vec3 temp_2 = Vec3.Subtraction(Temp_Vertex[4],Temp_Vertex[3]);
        Vec3 up_Vec = Vec3.AddOperation(temp_1,temp_2);
        Vec3 back_Vec = Vec3.Subtraction(temp_1,temp_2);

        if (Vec3.GetCosBetweenVector(Z_Vector,up_Vec) > 1/(2*this.imagingtimes))
        {
            Vec3[] middle=new Vec3[4];
            middle[0] = Temp_Vertex[4];
            middle[1] = Temp_Vertex[5];
            middle[2] = Temp_Vertex[6];
            middle[3] = Temp_Vertex[7];
            DrawBar_Straight(middle,BarColorArray[0],canvas);
        }
        else if (Vec3.GetCosBetweenVector(Z_Vector,up_Vec) < -1/(2*this.imagingtimes))
        {
            Vec3[] middle=new Vec3[4];
            middle[0] = Temp_Vertex[0];
            middle[1] = Temp_Vertex[1];
            middle[2] = Temp_Vertex[2];
            middle[3] = Temp_Vertex[3];
            DrawBar_Straight(middle,BarColorArray[2],canvas);
        }

        if (Vec3.GetCosBetweenVector(Z_Vector,back_Vec) > 1/(2*this.imagingtimes))
        {
            Vec3[] middle=new Vec3[4];
            middle[0] = Temp_Vertex[3];
            middle[1] = Temp_Vertex[2];
            middle[2] = Temp_Vertex[6];
            middle[3] = Temp_Vertex[7];
            DrawBar_Straight(middle,BarColorArray[1],canvas);
        }
        else if (Vec3.GetCosBetweenVector(Z_Vector,back_Vec) < -1/(2*this.imagingtimes))
        {
            Vec3[] middle=new Vec3[4];
            middle[0] = Temp_Vertex[0];
            middle[1] = Temp_Vertex[1];
            middle[2] = Temp_Vertex[5];
            middle[3] = Temp_Vertex[4];
            DrawBar_Straight(middle,BarColorArray[3],canvas);
        }

        if (Z_Vector.xx > 1/(2*imagingtimes))
        {
            Vec3[] middle=new Vec3[4];
            middle[0] = Temp_Vertex[1];
            middle[1] = Temp_Vertex[2];
            middle[2] = Temp_Vertex[6];
            middle[3] = Temp_Vertex[5];
            DrawSurface(middle,nineColor[1],canvas);
        }
        else if (Z_Vector.xx < -1/(2*imagingtimes))
        {
            Vec3[] middle=new Vec3[4];
            middle[0] = Temp_Vertex[0];
            middle[1] = Temp_Vertex[3];
            middle[2] = Temp_Vertex[7];
            middle[3] = Temp_Vertex[4];
            DrawSurface(middle,nineColor[0],canvas);
        }
    }

    void DrawLayer_Straight(Vec3[] Temp_Vertex,int[][] BarColorArray,int[][] nineColor,Canvas canvas)
    {
        Vec3 Z_Vector=Vec3.MultiplicationCross(X_Vector,Y_Vector);
        Vec3 temp_1 = Vec3.Subtraction(Temp_Vertex[5],Temp_Vertex[0]);
        Vec3 temp_2 = Vec3.Subtraction(Temp_Vertex[4],Temp_Vertex[1]);
        Vec3 up_Vec = Vec3.AddOperation(temp_1,temp_2);
        Vec3 right_Vec = Vec3.Subtraction(temp_1,temp_2);

        if (Vec3.GetCosBetweenVector(Z_Vector,up_Vec) > 1/(2*this.imagingtimes))
        {
            Vec3[] middle=new Vec3[4];
            middle[0] = Temp_Vertex[4];
            middle[1] = Temp_Vertex[5];
            middle[2] = Temp_Vertex[6];
            middle[3] = Temp_Vertex[7];
            DrawBar_Across(middle,BarColorArray[0],canvas);
        }
        else if (Vec3.GetCosBetweenVector(Z_Vector,up_Vec) < -1/(2*this.imagingtimes))
        {
            Vec3[] middle=new Vec3[4];
            middle[0] = Temp_Vertex[0];
            middle[1] = Temp_Vertex[1];
            middle[2] = Temp_Vertex[2];
            middle[3] = Temp_Vertex[3];
            DrawBar_Across(middle,BarColorArray[2],canvas);
        }

        if (Vec3.GetCosBetweenVector(Z_Vector,right_Vec) > 1/(2*this.imagingtimes))
        {
            Vec3[] middle=new Vec3[4];
            middle[0] = Temp_Vertex[1];
            middle[1] = Temp_Vertex[2];
            middle[2] = Temp_Vertex[6];
            middle[3] = Temp_Vertex[5];
            DrawBar_Straight(middle,BarColorArray[1],canvas);
        }
        else if (Vec3.GetCosBetweenVector(Z_Vector,right_Vec) < -1/(2*this.imagingtimes))
        {
            Vec3[] middle=new Vec3[4];
            middle[0] = Temp_Vertex[0];
            middle[1] = Temp_Vertex[3];
            middle[2] = Temp_Vertex[7];
            middle[3] = Temp_Vertex[4];
            DrawBar_Straight(middle,BarColorArray[3],canvas);
        }

        if (Z_Vector.yy > 1/(2*this.imagingtimes))
        {
            Vec3[] middle=new Vec3[4];
            middle[0] = Temp_Vertex[3];
            middle[1] = Temp_Vertex[2];
            middle[2] = Temp_Vertex[6];
            middle[3] = Temp_Vertex[7];
            DrawSurface(middle,nineColor[1],canvas);
        }
        else if (Z_Vector.yy < -1/(2*this.imagingtimes))
        {
            Vec3[] middle=new Vec3[4];
            middle[0] = Temp_Vertex[0];
            middle[1] = Temp_Vertex[1];
            middle[2] = Temp_Vertex[5];
            middle[3] = Temp_Vertex[4];
            DrawSurface(middle,nineColor[0],canvas);
        }
    }

    void DrawLayer_Vertical(Vec3[] Temp_Vertex,int[][] BarColorArray,int[][] nineColor,Canvas canvas)
    {
        Vec3 Z_Vector=Vec3.MultiplicationCross(X_Vector,Y_Vector);
        Vec3 temp_1 = Vec3.Subtraction(Temp_Vertex[2],Temp_Vertex[0]);
        Vec3 temp_2 = Vec3.Subtraction(Temp_Vertex[3],Temp_Vertex[1]);
        Vec3 back_Vec = Vec3.AddOperation(temp_1,temp_2);
        Vec3 right_Vec = Vec3.Subtraction(temp_1,temp_2);

        if (Vec3.GetCosBetweenVector(Z_Vector,back_Vec) > 1/(2*this.imagingtimes))
        {
            Vec3[] middle=new Vec3[4];
            middle[0] = Temp_Vertex[3];
            middle[1] = Temp_Vertex[2];
            middle[2] = Temp_Vertex[6];
            middle[3] = Temp_Vertex[7];
            DrawBar_Across(middle, BarColorArray[0],canvas);
        }
        else if (Vec3.GetCosBetweenVector(Z_Vector,back_Vec) < -1/(2*this.imagingtimes))
        {
            Vec3[] middle=new Vec3[4];
            middle[0] = Temp_Vertex[0];
            middle[1] = Temp_Vertex[1];
            middle[2] = Temp_Vertex[5];
            middle[3] = Temp_Vertex[4];
            DrawBar_Across(middle, BarColorArray[2],canvas);
        }

        if (Vec3.GetCosBetweenVector(Z_Vector,right_Vec) > 1/(2*this.imagingtimes))
        {
            Vec3[] middle=new Vec3[4];
            middle[0] = Temp_Vertex[1];
            middle[1] = Temp_Vertex[2];
            middle[2] = Temp_Vertex[6];
            middle[3] = Temp_Vertex[5];
            DrawBar_Across(middle, BarColorArray[1],canvas);
        }
        else if (Vec3.GetCosBetweenVector(Z_Vector,right_Vec) < -1/(2*this.imagingtimes))
        {
            Vec3[] middle=new Vec3[4];
            middle[0] = Temp_Vertex[0];
            middle[1] = Temp_Vertex[3];
            middle[2] = Temp_Vertex[7];
            middle[3] = Temp_Vertex[4];
            DrawBar_Across(middle, BarColorArray[3],canvas);
        }

        if (Z_Vector.zz > 1/(2*this.imagingtimes))
        {
            Vec3[] middle=new Vec3[4];
            middle[0] = Temp_Vertex[4];
            middle[1] = Temp_Vertex[5];
            middle[2] = Temp_Vertex[6];
            middle[3] = Temp_Vertex[7];
            DrawSurface(middle,nineColor[1],canvas);
        }
        else if (Z_Vector.zz < -1/(2*this.imagingtimes))
        {
            Vec3[] middle=new Vec3[4];
            middle[0] = Temp_Vertex[0];
            middle[1] = Temp_Vertex[1];
            middle[2] = Temp_Vertex[2];
            middle[3] = Temp_Vertex[3];
            DrawSurface(middle,nineColor[0],canvas);
        }
    }

    void CrossRotationAxis(Canvas canvas,int judge,double angle)
    {
        Vec3 Z_Vector=Vec3.MultiplicationCross(X_Vector,Y_Vector);
        // 0右1左2上3下4前5后
        Vec3 add_Across =Vec3.DivisionOperation(Vec3.Subtraction(Vertex_Rubik[1],Vertex_Rubik[0]),3);

        // 将魔方分成3份，更新旋转的那份的点值，同时一层一层画出魔方
        // 谢谢，重写的时候心情不好，原来的我是很有热情在写的，不应该忘记初衷
        Vec3[][] Temp_Vertex=new Vec3[3][8];
        for (int i = 0; i < 3; i++)
        {
            Temp_Vertex[i][0] = Vec3.AddOperation(Vertex_Rubik[0],Vec3.MultiplyOperation(add_Across,i));
            Temp_Vertex[i][1] = Vec3.AddOperation(Vertex_Rubik[0],Vec3.MultiplyOperation(add_Across,i+1));
            Temp_Vertex[i][2] = Vec3.AddOperation(Vertex_Rubik[3],Vec3.MultiplyOperation(add_Across,i+1));
            Temp_Vertex[i][3] = Vec3.AddOperation(Vertex_Rubik[3],Vec3.MultiplyOperation(add_Across,i));

            Temp_Vertex[i][4] = Vec3.AddOperation(Vertex_Rubik[4],Vec3.MultiplyOperation(add_Across,i));
            Temp_Vertex[i][5] = Vec3.AddOperation(Vertex_Rubik[4],Vec3.MultiplyOperation(add_Across,i+1));
            Temp_Vertex[i][6] = Vec3.AddOperation(Vertex_Rubik[7],Vec3.MultiplyOperation(add_Across,i+1));;
            Temp_Vertex[i][7] = Vec3.AddOperation(Vertex_Rubik[7],Vec3.MultiplyOperation(add_Across,i));;
        }

        // 更新旋转的那份魔方
        double r = sidelength_rc / Math.sqrt(2);
        double Cos_A=Math.cos(angle);
        double Sin_A=Math.sin(angle);
        for (int i = 0; i < 8; i++)
        {
            double Cos_P = Temp_Vertex[judge][i].yy / r;
            double Sin_P = Temp_Vertex[judge][i].zz / r;
            double Cos_Final = Cos_P * Cos_A + Sin_P * Sin_A;
            double Sin_Final = Sin_P * Cos_A - Sin_A * Cos_P;
            Temp_Vertex[judge][i].yy = Cos_Final * r;
            Temp_Vertex[judge][i].zz = Sin_Final * r;
        }

        // 填充颜色并且三层画完
        int[][] BarColorArray=new int[4][3];
        int[][] nineColor=new int[2][9];
        if(Z_Vector.xx>0) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) BarColorArray[0][j] = SurfaceColor[2][j * 3 + i];
                for (int j = 0; j < 3; j++) BarColorArray[1][j] = SurfaceColor[5][j * 3 + i];
                for (int j = 0; j < 3; j++) BarColorArray[2][j] = SurfaceColor[3][j * 3 + i];
                for (int j = 0; j < 3; j++) BarColorArray[3][j] = SurfaceColor[4][j * 3 + i];
                for (int j = 0; j < 9; j++) {
                    if (i == 0) nineColor[0][j] = SurfaceColor[1][j];
                    else nineColor[0][j] = 0xff000000;
                    if (i == 2) nineColor[1][j] = SurfaceColor[0][j];
                    else nineColor[1][j] = 0xff000000;
                }
                DrawLayer_Across(Temp_Vertex[i], BarColorArray, nineColor, canvas);
            }
        }
        else
        {
            for (int i = 2; i >= 0; i--) {
                for (int j = 0; j < 3; j++) BarColorArray[0][j] = SurfaceColor[2][j * 3 + i];
                for (int j = 0; j < 3; j++) BarColorArray[1][j] = SurfaceColor[5][j * 3 + i];
                for (int j = 0; j < 3; j++) BarColorArray[2][j] = SurfaceColor[3][j * 3 + i];
                for (int j = 0; j < 3; j++) BarColorArray[3][j] = SurfaceColor[4][j * 3 + i];
                for (int j = 0; j < 9; j++) {
                    if (i == 0) nineColor[0][j] = SurfaceColor[1][j];
                    else nineColor[0][j] = 0xff000000;
                    if (i == 2) nineColor[1][j] = SurfaceColor[0][j];
                    else nineColor[1][j] = 0xff000000;
                }
                DrawLayer_Across(Temp_Vertex[i], BarColorArray, nineColor, canvas);
            }
        }
    }

    void StraightRotationAxis(Canvas canvas,int judge,double angle)
    {
        // 0右1左2上3下4前5后
        Vec3 Z_Vector=Vec3.MultiplicationCross(X_Vector,Y_Vector);
        Vec3 add_Straight = Vec3.DivisionOperation(Vec3.Subtraction(Vertex_Rubik[3],Vertex_Rubik[0]),3);

        // 将魔方分成3份，更新旋转的那份的点值，同时一层一层画出魔方
        Vec3[][] Temp_Vertex=new Vec3[3][8];
        for (int i = 0; i < 3; i++)
        {
            Temp_Vertex[i][0] = Vec3.AddOperation(Vertex_Rubik[0],Vec3.MultiplyOperation(add_Straight,i));
            Temp_Vertex[i][1] = Vec3.AddOperation(Vertex_Rubik[1],Vec3.MultiplyOperation(add_Straight,i));
            Temp_Vertex[i][2] = Vec3.AddOperation(Vertex_Rubik[1],Vec3.MultiplyOperation(add_Straight,i+1));
            Temp_Vertex[i][3] = Vec3.AddOperation(Vertex_Rubik[0],Vec3.MultiplyOperation(add_Straight,i+1));

            Temp_Vertex[i][4] = Vec3.AddOperation(Vertex_Rubik[4],Vec3.MultiplyOperation(add_Straight,i));
            Temp_Vertex[i][5] = Vec3.AddOperation(Vertex_Rubik[5],Vec3.MultiplyOperation(add_Straight,i));
            Temp_Vertex[i][6] = Vec3.AddOperation(Vertex_Rubik[5],Vec3.MultiplyOperation(add_Straight,i+1));;
            Temp_Vertex[i][7] = Vec3.AddOperation(Vertex_Rubik[4],Vec3.MultiplyOperation(add_Straight,i+1));;
        }

        // 更新旋转的那份魔方
        double r = sidelength_rc / Math.sqrt(2);
        double Cos_A=Math.cos(angle);
        double Sin_A=Math.sin(angle);
        for (int i = 0; i < 8; i++)
        {
            double Cos_P = Temp_Vertex[judge][i].xx / r;
            double Sin_P = Temp_Vertex[judge][i].zz / r;
            double Cos_Final = Cos_P * Cos_A + Sin_P * Sin_A;
            double Sin_Final = Sin_P * Cos_A - Sin_A * Cos_P;
            Temp_Vertex[judge][i].xx = Cos_Final * r;
            Temp_Vertex[judge][i].zz = Sin_Final * r;
        }

        // 填充颜色并且三层画完
        int[][] BarColorArray=new int[4][3];
        int[][] nineColor=new int[2][9];
        if(Z_Vector.yy>0.0) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) BarColorArray[0][j] = SurfaceColor[2][j + i * 3];
                for (int j = 0; j < 3; j++) BarColorArray[1][j] = SurfaceColor[0][j * 3 + i];
                for (int j = 0; j < 3; j++) BarColorArray[2][j] = SurfaceColor[3][j + i * 3];
                for (int j = 0; j < 3; j++) BarColorArray[3][j] = SurfaceColor[1][j * 3 + i];
                for (int j = 0; j < 9; j++) {
                    if (i == 0) nineColor[0][j] = SurfaceColor[4][j];
                    else nineColor[0][j] = 0xff000000;
                    if (i == 2) nineColor[1][j] = SurfaceColor[5][j];
                    else nineColor[1][j] = 0xff000000;
                }
                DrawLayer_Straight(Temp_Vertex[i], BarColorArray, nineColor, canvas);
            }
        }
        else
        {
            for (int i = 2; i >= 0; i--) {
                for (int j = 0; j < 3; j++) BarColorArray[0][j] = SurfaceColor[2][j + i * 3];
                for (int j = 0; j < 3; j++) BarColorArray[1][j] = SurfaceColor[0][j * 3 + i];
                for (int j = 0; j < 3; j++) BarColorArray[2][j] = SurfaceColor[3][j + i * 3];
                for (int j = 0; j < 3; j++) BarColorArray[3][j] = SurfaceColor[1][j * 3 + i];
                for (int j = 0; j < 9; j++) {
                    if (i == 0) nineColor[0][j] = SurfaceColor[4][j];
                    else nineColor[0][j] = 0xff000000;
                    if (i == 2) nineColor[1][j] = SurfaceColor[5][j];
                    else nineColor[1][j] = 0xff000000;
                }
                DrawLayer_Straight(Temp_Vertex[i], BarColorArray, nineColor, canvas);
            }
        }
    }

    void VerticalRotationAxis(Canvas canvas,int judge,double angle)
    {
        // 0右1左2上3下4前5后
        Vec3 Z_Vector=Vec3.MultiplicationCross(X_Vector,Y_Vector);
        Vec3 add_Vertical = Vec3.DivisionOperation(Vec3.Subtraction(Vertex_Rubik[4],Vertex_Rubik[0]),3);

        // 将魔方分成3份，更新旋转的那份的点值，同时一层一层画出魔方
        Vec3[][] Temp_Vertex=new Vec3[3][8];
        for (int i = 0; i < 3; i++)
        {
            Temp_Vertex[i][0] = Vec3.AddOperation(Vertex_Rubik[0],Vec3.MultiplyOperation(add_Vertical,i));
            Temp_Vertex[i][1] = Vec3.AddOperation(Vertex_Rubik[1],Vec3.MultiplyOperation(add_Vertical,i));
            Temp_Vertex[i][2] = Vec3.AddOperation(Vertex_Rubik[2],Vec3.MultiplyOperation(add_Vertical,i));
            Temp_Vertex[i][3] = Vec3.AddOperation(Vertex_Rubik[3],Vec3.MultiplyOperation(add_Vertical,i));

            Temp_Vertex[i][4] = Vec3.AddOperation(Vertex_Rubik[0],Vec3.MultiplyOperation(add_Vertical,i+1));
            Temp_Vertex[i][5] = Vec3.AddOperation(Vertex_Rubik[1],Vec3.MultiplyOperation(add_Vertical,i+1));
            Temp_Vertex[i][6] = Vec3.AddOperation(Vertex_Rubik[2],Vec3.MultiplyOperation(add_Vertical,i+1));;
            Temp_Vertex[i][7] = Vec3.AddOperation(Vertex_Rubik[3],Vec3.MultiplyOperation(add_Vertical,i+1));;
        }

        // 更新旋转的那份魔方
        double r = sidelength_rc / Math.sqrt(2);
        double Cos_A=Math.cos(angle);
        double Sin_A=Math.sin(angle);
        for (int i = 0; i < 8; i++)
        {
            double Cos_P = Temp_Vertex[judge][i].xx / r;
            double Sin_P = Temp_Vertex[judge][i].yy / r;
            double Cos_Final = Cos_P * Cos_A + Sin_P * Sin_A;
            double Sin_Final = Sin_P * Cos_A - Sin_A * Cos_P;
            Temp_Vertex[judge][i].xx = Cos_Final * r;
            Temp_Vertex[judge][i].yy = Sin_Final * r;
        }

        // 填充颜色并且三层画完
        int[][] BarColorArray=new int[4][3];
        int[][] nineColor=new int[2][9];
        if(Z_Vector.zz>0.0) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) BarColorArray[0][j] = SurfaceColor[5][j + i * 3];
                for (int j = 0; j < 3; j++) BarColorArray[1][j] = SurfaceColor[0][j + i * 3];
                for (int j = 0; j < 3; j++) BarColorArray[2][j] = SurfaceColor[4][j + i * 3];
                for (int j = 0; j < 3; j++) BarColorArray[3][j] = SurfaceColor[1][j + i * 3];
                for (int j = 0; j < 9; j++) {
                    if (i == 0) nineColor[0][j] = SurfaceColor[3][j];
                    else nineColor[0][j] = 0xff000000;
                    if (i == 2) nineColor[1][j] = SurfaceColor[2][j];
                    else nineColor[1][j] = 0xff000000;
                }
                DrawLayer_Vertical(Temp_Vertex[i], BarColorArray, nineColor, canvas);
            }
        }
        else
        {
            for (int i = 2; i >= 0; i--) {
                for (int j = 0; j < 3; j++) BarColorArray[0][j] = SurfaceColor[5][j + i * 3];
                for (int j = 0; j < 3; j++) BarColorArray[1][j] = SurfaceColor[0][j + i * 3];
                for (int j = 0; j < 3; j++) BarColorArray[2][j] = SurfaceColor[4][j + i * 3];
                for (int j = 0; j < 3; j++) BarColorArray[3][j] = SurfaceColor[1][j + i * 3];
                for (int j = 0; j < 9; j++) {
                    if (i == 0) nineColor[0][j] = SurfaceColor[3][j];
                    else nineColor[0][j] = 0xff000000;
                    if (i == 2) nineColor[1][j] = SurfaceColor[2][j];
                    else nineColor[1][j] = 0xff000000;
                }
                DrawLayer_Vertical(Temp_Vertex[i], BarColorArray, nineColor, canvas);
            }
        }
    }

    // 画正在旋转中的魔方
    void DrawRotationCube(Canvas canvas)
    {
        // 0x1y2z
        // 0右1左2上3下4前5后
        if(mRotationPoint.RotationiAxis==0)
        {
            CrossRotationAxis(canvas,mRotationPoint.RotateJudge,rotateAngle);
        }
        else if(mRotationPoint.RotationiAxis==1)
        {
            StraightRotationAxis(canvas,mRotationPoint.RotateJudge,rotateAngle);
        }
        else if(mRotationPoint.RotationiAxis==2)
        {
            VerticalRotationAxis(canvas,mRotationPoint.RotateJudge,rotateAngle);
        }
        else DrawCube(canvas);
    }

    void Animate()      // 给魔方旋转加点动画效果，手感更好
    {

    }

    boolean isInSurface(Vec2 place,Vec3[] Surface,Vec2 pericenter)
    {
        Vec2[] Surface_2D=new Vec2[4];
        Surface_2D[0]=Vec2.AddOperation(Transform3DTo2D(Surface[0]),pericenter);
        Surface_2D[1]=Vec2.AddOperation(Transform3DTo2D(Surface[1]),pericenter);
        Surface_2D[2]=Vec2.AddOperation(Transform3DTo2D(Surface[2]),pericenter);
        Surface_2D[3]=Vec2.AddOperation(Transform3DTo2D(Surface[3]),pericenter);

        Vec2 vec_0 = Vec2.Subtraction(Surface_2D[1],Surface_2D[0]);
        Vec2 vec_1 = Vec2.Subtraction(Surface_2D[3],Surface_2D[0]);
        Vec2 vec_2 = new Vec2(place.xx - Surface_2D[0].xx, place.yy - Surface_2D[0].yy);
        double standard=Vec2.GetCosBetweenVector(vec_0,vec_1);
        if (Vec2.GetCosBetweenVector(vec_2,vec_0) < standard || Vec2.GetCosBetweenVector(vec_2,vec_1) < standard)
            return false;

        vec_0 = Vec2.Subtraction(Surface_2D[0],Surface_2D[1]);
        vec_1 = Vec2.Subtraction(Surface_2D[2],Surface_2D[1]);
        vec_2 = new Vec2(place.xx - Surface_2D[1].xx, place.yy - Surface_2D[1].yy);
        standard=Vec2.GetCosBetweenVector(vec_0,vec_1);
        if (Vec2.GetCosBetweenVector(vec_2,vec_0) < standard || Vec2.GetCosBetweenVector(vec_2,vec_1) < standard)
            return false;

        vec_0 = Vec2.Subtraction(Surface_2D[1],Surface_2D[2]);
        vec_1 = Vec2.Subtraction(Surface_2D[3],Surface_2D[2]);
        vec_2 = new Vec2(place.xx - Surface_2D[2].xx, place.yy - Surface_2D[2].yy);
        standard=Vec2.GetCosBetweenVector(vec_0,vec_1);
        if (Vec2.GetCosBetweenVector(vec_2,vec_0) < standard || Vec2.GetCosBetweenVector(vec_2,vec_1) < standard)
            return false;

        vec_0 = Vec2.Subtraction(Surface_2D[2],Surface_2D[3]);
        vec_1 = Vec2.Subtraction(Surface_2D[0],Surface_2D[3]);
        vec_2 = new Vec2(place.xx - Surface_2D[3].xx, place.yy - Surface_2D[3].yy);
        standard=Vec2.GetCosBetweenVector(vec_0,vec_1);
        if (Vec2.GetCosBetweenVector(vec_2,vec_0) < standard || Vec2.GetCosBetweenVector(vec_2,vec_1) < standard)
            return false;
        return true;
    }

    int GetIndexInSurface(Vec2 place,Vec3[] Surface,Vec2 pericenter)
    {
        int n = 0, m = 0;		// n 是 y 轴上的坐标，m 是 x 轴上的坐标
        Vec3[] Surface_Temp=new Vec3[4];
        Vec3 add_X = Vec3.DivisionOperation(Vec3.Subtraction(Surface[1],Surface[0]),3);
        Vec3 add_Y = Vec3.DivisionOperation(Vec3.Subtraction(Surface[3],Surface[0]),3);
        for (n=0; n < 3; n++)
        {
            Surface_Temp[0] = Vec3.AddOperation(Surface[0],Vec3.MultiplyOperation(add_Y, n));
            Surface_Temp[1] = Vec3.AddOperation(Surface[1],Vec3.MultiplyOperation(add_Y, n));
            Surface_Temp[2] = Vec3.AddOperation(Surface[1],Vec3.MultiplyOperation(add_Y, n+1));
            Surface_Temp[3] = Vec3.AddOperation(Surface[0],Vec3.MultiplyOperation(add_Y, n+1));
            if (isInSurface(place, Surface_Temp, pericenter))break;
        }
        for (m=0; m < 3; m++)
        {
            Surface_Temp[0] = Vec3.AddOperation(Surface[0],Vec3.MultiplyOperation(add_X, m));
            Surface_Temp[1] = Vec3.AddOperation(Surface[3],Vec3.MultiplyOperation(add_X, m));
            Surface_Temp[2] = Vec3.AddOperation(Surface[3],Vec3.MultiplyOperation(add_X, m+1));
            Surface_Temp[3] = Vec3.AddOperation(Surface[0],Vec3.MultiplyOperation(add_X, m+1));
            if (isInSurface(place, Surface_Temp, pericenter))break;
        }
        if(n>=3||m>=3)return -1;
        return n * 3 + m;
    }

    CubeIndex GetCubeIndex(Vec2 place,Vec2 pericenter)
    {
        // 0右1左2上3下4前5后
        CubeIndex result=new CubeIndex();
        Vec3 Z_Vector=Vec3.MultiplicationCross(X_Vector,Y_Vector);
        Vec3[] temp_Vertex=new Vec3[4];
        boolean isFind=false;
        // 0右1左2上3下4前5后
        // 判断右面能不能贴出
        if(Z_Vector.xx>1/(imagingtimes*2))
        {
            temp_Vertex[0]=Vertex_Rubik[1];
            temp_Vertex[1]=Vertex_Rubik[2];
            temp_Vertex[2]=Vertex_Rubik[6];
            temp_Vertex[3]=Vertex_Rubik[5];
            if(isInSurface(place,temp_Vertex,pericenter))
            {
                isFind=true;
                result.plane=0;
                result.index=GetIndexInSurface(place,temp_Vertex,pericenter);
            }
        }
        // 判断左面能不能贴出
        else if(Z_Vector.xx<-1/(imagingtimes*2))
        {
            temp_Vertex[0]=Vertex_Rubik[0];
            temp_Vertex[1]=Vertex_Rubik[3];
            temp_Vertex[2]=Vertex_Rubik[7];
            temp_Vertex[3]=Vertex_Rubik[4];
            if(isInSurface(place,temp_Vertex,pericenter))
            {
                isFind=true;
                result.plane=1;
                result.index=GetIndexInSurface(place,temp_Vertex,pericenter);
            }
        }
        if(isFind)return result;

        // 判断上面能不能贴出
        if(Z_Vector.zz>1/(imagingtimes*2))
        {
            temp_Vertex[0]=Vertex_Rubik[4];
            temp_Vertex[1]=Vertex_Rubik[5];
            temp_Vertex[2]=Vertex_Rubik[6];
            temp_Vertex[3]=Vertex_Rubik[7];
            if(isInSurface(place,temp_Vertex,pericenter))
            {
                isFind=true;
                result.plane=2;
                result.index=GetIndexInSurface(place,temp_Vertex,pericenter);
            }
        }
        // 判断下面能不能贴出
        else if(Z_Vector.zz<-1/(imagingtimes*2))
        {
            temp_Vertex[0]=Vertex_Rubik[0];
            temp_Vertex[1]=Vertex_Rubik[1];
            temp_Vertex[2]=Vertex_Rubik[2];
            temp_Vertex[3]=Vertex_Rubik[3];
            if(isInSurface(place,temp_Vertex,pericenter))
            {
                isFind=true;
                result.plane=3;
                result.index=GetIndexInSurface(place,temp_Vertex,pericenter);
            }
        }
        if (isFind)return result;

        // 判断前面能不能贴出
        if(Z_Vector.yy<-1/(imagingtimes*2))
        {
            temp_Vertex[0]=Vertex_Rubik[0];
            temp_Vertex[1]=Vertex_Rubik[1];
            temp_Vertex[2]=Vertex_Rubik[5];
            temp_Vertex[3]=Vertex_Rubik[4];
            if(isInSurface(place,temp_Vertex,pericenter))
            {
                isFind=true;
                result.plane=4;
                result.index=GetIndexInSurface(place,temp_Vertex,pericenter);
            }
        }
        // 判断后面能不能贴出
        else if(Z_Vector.yy>1/(imagingtimes*2))
        {
            temp_Vertex[0]=Vertex_Rubik[3];
            temp_Vertex[1]=Vertex_Rubik[2];
            temp_Vertex[2]=Vertex_Rubik[6];
            temp_Vertex[3]=Vertex_Rubik[7];
            if(isInSurface(place,temp_Vertex,pericenter))
            {
                isFind=true;
                result.plane=5;
                result.index=GetIndexInSurface(place,temp_Vertex,pericenter);
            }
        }
        if (isFind)return result;
        result.index=-1;
        result.plane=-1;
        return result;
    }

    RotationPoint GetRotationPoint(CubeIndex cubeIndex,Vec3 moveVec)
    {
        RotationPoint result=new RotationPoint();
        result.RotateJudge=-1;
        result.RotationiAxis=-1;
        int x=cubeIndex.index%3;
        int y=cubeIndex.index/3;
        // 0右1左2上3下4前5后
        // 0x1y2z
        if(cubeIndex.plane==2||cubeIndex.plane==3)
        {
            if (Math.abs(moveVec.xx) > Math.abs(moveVec.yy) && Math.abs(moveVec.xx) > 20)
            {
                result.RotationiAxis=1;
                result.RotateJudge=y;
            }
            else if (Math.abs(moveVec.xx) <= Math.abs(moveVec.yy) && Math.abs(moveVec.yy) > 20)
            {
                result.RotationiAxis=0;
                result.RotateJudge=x;
            }
        }
        else if(cubeIndex.plane==0||cubeIndex.plane==1)
        {
            if (Math.abs(moveVec.yy) > Math.abs(moveVec.zz) && Math.abs(moveVec.yy) > 20)
            {
                result.RotationiAxis=2;
                result.RotateJudge=y;
            }
            else if (Math.abs(moveVec.yy) <= Math.abs(moveVec.zz) && Math.abs(moveVec.zz) > 20)
            {
                result.RotationiAxis=1;
                result.RotateJudge=x;
            }
        }
        else if(cubeIndex.plane==4||cubeIndex.plane==5)
        {
            if (Math.abs(moveVec.xx) > Math.abs(moveVec.zz) && Math.abs(moveVec.xx) > 20)
            {
                result.RotationiAxis=2;
                result.RotateJudge=y;
            }
            else if (Math.abs(moveVec.xx) <= Math.abs(moveVec.zz) && Math.abs(moveVec.zz) > 20)
            {
                result.RotationiAxis=0;
                result.RotateJudge=x;
            }
        }
        return result;
    }

    // 这里可能会有拧动魔方旋转方向相反的答案
    boolean isRotationOpposite_Across()
    {
        Vec3 Z_Vector=Vec3.MultiplicationCross(X_Vector,Y_Vector);
        if(Z_Vector.zz<0.0)return true;
        return false;
    }
    boolean isRotationOpposite_Straight()
    {
        Vec3 Z_Vector=Vec3.MultiplicationCross(X_Vector,Y_Vector);
        if(Z_Vector.zz<0.0)return true;
        return false;
    }
    boolean isRotationOpposite_Vertical()
    {
        Vec3 Z_Vector=Vec3.MultiplicationCross(X_Vector,Y_Vector);
        if(Z_Vector.yy<0.0)return true;
        return false;
    }

    // 顺时针旋转一个数组
    void SurfaceRotation_Clockwise(int plane)
    {
        int[] index_middle = { 3, 7, 5 };   // 中心块
        int[] index_angle = { 6, 8, 2 };    // 角块
        for (int i = 0; i < 3; i++)
        {
            int temp = SurfaceColor[plane][index_middle[i]];
            SurfaceColor[plane][index_middle[i]] = SurfaceColor[plane][1];
            SurfaceColor[plane][1] = temp;
            temp = SurfaceColor[plane][index_angle[i]];
            SurfaceColor[plane][index_angle[i]] = SurfaceColor[plane][0];
            SurfaceColor[plane][0] = temp;
        }
    }
    // 逆时针旋转一个数组
    void SurfaceRotation_Counterclockwise(int plane)
    {
        int[] index_middle = { 3, 7, 5 };   // 中心块
        int[] index_angle = { 6, 8, 2 };    // 角块
        for (int i = 2; i >= 0; i--)
        {
            int temp = SurfaceColor[plane][index_middle[i]];
            SurfaceColor[plane][index_middle[i]] = SurfaceColor[plane][1];
            SurfaceColor[plane][1] = temp;
            temp = SurfaceColor[plane][index_angle[i]];
            SurfaceColor[plane][index_angle[i]] = SurfaceColor[plane][0];
            SurfaceColor[plane][0] = temp;
        }
    }

    // 对于一个颜色数组进行首尾颠倒
    void ReverseColorArray(int[] array_color)
    {
        int len=array_color.length;
        for (int i = 0; i < (len >> 1); i++)
        {
            int temp = array_color[i];
            array_color[i] = array_color[len - i - 1];
            array_color[len - i - 1] = temp;
        }
    }

    // 0右1左2上3下4前5后
    // 关于 x 轴旋转的某一层的颜色数组旋转
    void rotateLayer_Across_Clockwise(int judge)
    {
        // 0右1左2上3下4前5后
        int[][] Temp=new int[4][3];
        for (int i = 0; i < 3; i++)Temp[0][i] = SurfaceColor[2][i * 3 + judge];
        for (int i = 0; i < 3; i++)Temp[1][i] = SurfaceColor[5][i * 3 + judge];
        for (int i = 0; i < 3; i++)Temp[2][i] = SurfaceColor[3][i * 3 + judge];
        for (int i = 0; i < 3; i++)Temp[3][i] = SurfaceColor[4][i * 3 + judge];

        ReverseColorArray(Temp[0]);
        ReverseColorArray(Temp[2]);
        for (int i = 0; i < 3; i++) SurfaceColor[2][i * 3 + judge] = Temp[3][i];
        for (int i = 0; i < 3; i++)SurfaceColor[5][i * 3 + judge] = Temp[0][i];
        for (int i = 0; i < 3; i++)SurfaceColor[3][i * 3 + judge] = Temp[1][i];
        for (int i = 0; i < 3; i++)SurfaceColor[4][i * 3 + judge] = Temp[2][i];
        if(judge==0)SurfaceRotation_Clockwise(1);
        else if(judge==2)SurfaceRotation_Clockwise(0);
    }
    void rotateLayer_Across_Counterclockwise(int judge)
    {
        // 0右1左2上3下4前5后
        int[][] Temp=new int[4][3];
        for (int i = 0; i < 3; i++)Temp[0][i] = SurfaceColor[2][i * 3 + judge];
        for (int i = 0; i < 3; i++)Temp[1][i] = SurfaceColor[5][i * 3 + judge];
        for (int i = 0; i < 3; i++)Temp[2][i] = SurfaceColor[3][i * 3 + judge];
        for (int i = 0; i < 3; i++)Temp[3][i] = SurfaceColor[4][i * 3 + judge];

        ReverseColorArray(Temp[1]);
        ReverseColorArray(Temp[3]);
        for (int i = 0; i < 3; i++) SurfaceColor[2][i * 3 + judge] = Temp[1][i];
        for (int i = 0; i < 3; i++)SurfaceColor[5][i * 3 + judge] = Temp[2][i];
        for (int i = 0; i < 3; i++)SurfaceColor[3][i * 3 + judge] = Temp[3][i];
        for (int i = 0; i < 3; i++)SurfaceColor[4][i * 3 + judge] = Temp[0][i];

        if(judge==0)SurfaceRotation_Counterclockwise(1);
        else if(judge==2)SurfaceRotation_Counterclockwise(0);
    }

    void rotateLayer_Straight_Clockwise(int judge)
    {
        // 0右1左2上3下4前5后
        int[][] Temp=new int[4][3];
        for (int i = 0; i < 3; i++)Temp[0][i] = SurfaceColor[2][i + judge * 3];
        for (int i = 0; i < 3; i++)Temp[1][i] = SurfaceColor[0][i * 3 + judge];
        for (int i = 0; i < 3; i++)Temp[2][i] = SurfaceColor[3][i + judge * 3];
        for (int i = 0; i < 3; i++)Temp[3][i] = SurfaceColor[1][i * 3 + judge];

        ReverseColorArray(Temp[0]);
        ReverseColorArray(Temp[2]);
        for (int i = 0; i < 3; i++) SurfaceColor[2][i + judge * 3] = Temp[3][i];
        for (int i = 0; i < 3; i++)SurfaceColor[0][i * 3 + judge] = Temp[0][i];
        for (int i = 0; i < 3; i++)SurfaceColor[3][i + judge * 3] = Temp[1][i];
        for (int i = 0; i < 3; i++)SurfaceColor[1][i * 3 + judge] = Temp[2][i];
        if(judge==0)SurfaceRotation_Clockwise(4);
        else if(judge==2)SurfaceRotation_Clockwise(5);
    }
    void rotateLayer_Straight_Counterclockwise(int judge)
    {
        // 0右1左2上3下4前5后
        int[][] Temp=new int[4][3];
        for (int i = 0; i < 3; i++)Temp[0][i] = SurfaceColor[2][i + judge * 3];
        for (int i = 0; i < 3; i++)Temp[1][i] = SurfaceColor[0][i * 3 + judge];
        for (int i = 0; i < 3; i++)Temp[2][i] = SurfaceColor[3][i + judge * 3];
        for (int i = 0; i < 3; i++)Temp[3][i] = SurfaceColor[1][i * 3 + judge];

        ReverseColorArray(Temp[1]);
        ReverseColorArray(Temp[3]);
        for (int i = 0; i < 3; i++) SurfaceColor[2][i + judge * 3] = Temp[1][i];
        for (int i = 0; i < 3; i++)SurfaceColor[0][i * 3 + judge] = Temp[2][i];
        for (int i = 0; i < 3; i++)SurfaceColor[3][i + judge * 3] = Temp[3][i];
        for (int i = 0; i < 3; i++)SurfaceColor[1][i * 3 + judge] = Temp[0][i];
        if(judge==0)SurfaceRotation_Counterclockwise(4);
        else if(judge==2)SurfaceRotation_Counterclockwise(5);
    }

    void rotateLayer_Vertical_Clockwise(int judge)
    {
        // 0右1左2上3下4前5后
        int[][] Temp=new int[4][3];
        for (int i = 0; i < 3; i++)Temp[0][i] = SurfaceColor[5][i + judge * 3];
        for (int i = 0; i < 3; i++)Temp[1][i] = SurfaceColor[0][i + judge * 3];
        for (int i = 0; i < 3; i++)Temp[2][i] = SurfaceColor[4][i + judge * 3];
        for (int i = 0; i < 3; i++)Temp[3][i] = SurfaceColor[1][i + judge * 3];

        ReverseColorArray(Temp[0]);
        ReverseColorArray(Temp[2]);
        for (int i = 0; i < 3; i++) SurfaceColor[5][i + judge * 3] = Temp[3][i];
        for (int i = 0; i < 3; i++)SurfaceColor[0][i + judge * 3] = Temp[0][i];
        for (int i = 0; i < 3; i++)SurfaceColor[4][i + judge * 3] = Temp[1][i];
        for (int i = 0; i < 3; i++)SurfaceColor[1][i + judge * 3] = Temp[2][i];
        if(judge==0)SurfaceRotation_Clockwise(3);
        else if(judge==2)SurfaceRotation_Clockwise(2);
    }
    void rotateLayer_Vertical_Counterclockwise(int judge)
    {
        // 0右1左2上3下4前5后
        int[][] Temp=new int[4][3];
        for (int i = 0; i < 3; i++)Temp[0][i] = SurfaceColor[5][i + judge * 3];
        for (int i = 0; i < 3; i++)Temp[1][i] = SurfaceColor[0][i + judge * 3];
        for (int i = 0; i < 3; i++)Temp[2][i] = SurfaceColor[4][i + judge * 3];
        for (int i = 0; i < 3; i++)Temp[3][i] = SurfaceColor[1][i + judge * 3];

        ReverseColorArray(Temp[1]);
        ReverseColorArray(Temp[3]);
        for (int i = 0; i < 3; i++) SurfaceColor[5][i + judge * 3] = Temp[1][i];
        for (int i = 0; i < 3; i++)SurfaceColor[0][i + judge * 3] = Temp[2][i];
        for (int i = 0; i < 3; i++)SurfaceColor[4][i + judge * 3] = Temp[3][i];
        for (int i = 0; i < 3; i++)SurfaceColor[1][i + judge * 3] = Temp[0][i];
        if(judge==0)SurfaceRotation_Counterclockwise(3);
        else if(judge==2)SurfaceRotation_Counterclockwise(2);
    }
}
