package com.example.rubikcube;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

// draw的时候只需要颜色数组，旋转状态，XY值就够了
public class GameView extends View
{
    private String TAG="hxd";
    RubikCube rubikCube;
    RubikCube.Vec2 lastpoint;
    RubikCube.Vec2 pericenter;
    public GameView(Context context) {
        super(context);
        double width=context.getResources().getDisplayMetrics().widthPixels;
        double height=context.getResources().getDisplayMetrics().heightPixels;
        Log.e(TAG, "屏幕大小: "+width+","+height);
        rubikCube=new RubikCube(Math.min(width,height)*0.5);
        lastpoint=new RubikCube.Vec2();
        pericenter=new RubikCube.Vec2(width/2,height/2);
    }

    @Override
    public void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        pericenter.xx=canvas.getWidth()/2;
        pericenter.yy=canvas.getHeight()/2;
        if(rubikCube.isEntirety)
        rubikCube.DrawCube(canvas);
        else rubikCube.DrawRotationCube(canvas);
    }

    public void ReciveMessage(MotionEvent event)
    {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN: {
                lastpoint.xx = event.getX();
                lastpoint.yy = event.getY();
                RubikCube.CubeIndex temp = rubikCube.GetCubeIndex(new RubikCube.Vec2(event.getX(), event.getY()), pericenter);
                if (temp.plane != -1 && temp.index != -1) {
                    rubikCube.mCubeIndex = temp;
                    rubikCube.isEntirety = false;
                } else rubikCube.isEntirety = true;
                break;
            }
            /**
             * 触屏实时位置
             */
            case MotionEvent.ACTION_MOVE: {
                // 2π/1000
                double change_x = event.getX() - lastpoint.xx;
                double change_y = event.getY() - lastpoint.yy;
                if (rubikCube.isEntirety) {
                    if (Math.abs(change_x) > 10) {
                        if (change_x > 0)
                            rubikCube.RotateEntirety(Math.toRadians(6), true);
                        else rubikCube.RotateEntirety(Math.toRadians(-6), true);
                        lastpoint.xx = event.getX();
                    }
                    if (Math.abs(change_y) > 10) {
                        if (change_y > 0)
                            rubikCube.RotateEntirety(Math.toRadians(6), false);
                        else rubikCube.RotateEntirety(Math.toRadians(-6), false);
                        lastpoint.yy = event.getY();
                    }
                } else {
                    if(rubikCube.mRotationPoint.RotationiAxis==-1||rubikCube.mRotationPoint.RotateJudge==-1) {
                        RubikCube.Vec3 temp = RubikCube.Vec3.AddOperation(
                                RubikCube.Vec3.MultiplyOperation(rubikCube.X_Vector, change_x),
                                RubikCube.Vec3.MultiplyOperation(rubikCube.Y_Vector, change_y));
                        rubikCube.mRotationPoint = rubikCube.GetRotationPoint(rubikCube.mCubeIndex, temp);
                    }
                    else{
                        RubikCube.Vec3 moveVec = RubikCube.Vec3.AddOperation(
                                RubikCube.Vec3.MultiplyOperation(rubikCube.X_Vector, change_x),
                                RubikCube.Vec3.MultiplyOperation(rubikCube.Y_Vector, change_y));
                        if(rubikCube.mRotationPoint.RotationiAxis==0)   // 关于 x 轴旋转
                        {
                            RubikCube.Vec2 projection = new RubikCube.Vec2(moveVec.yy, moveVec.zz);
                            double Temp_Rotation = projection.GetMouldLength() / (rubikCube.sidelength_rc/9);
                            if (projection.xx < 0.0)Temp_Rotation = -Temp_Rotation;	// 如果是逆时针旋转
                            if (rubikCube.isRotationOpposite_Across())Temp_Rotation = -Temp_Rotation;	// 如果要颠倒
                            rubikCube.rotateAngle=Math.toRadians(Temp_Rotation*5);
                        }
                        else if (rubikCube.mRotationPoint.RotationiAxis==1)	// 关于 y 轴旋转
                        {
                            RubikCube.Vec2 projection = new RubikCube.Vec2(moveVec.xx, moveVec.zz);
                            double Temp_Rotation = projection.GetMouldLength() / (rubikCube.sidelength_rc/9);
                            if (projection.xx < 0.0)Temp_Rotation = -Temp_Rotation;	// 如果是逆时针旋转
                            if (rubikCube.isRotationOpposite_Straight())Temp_Rotation = -Temp_Rotation;	// 如果要颠倒
                            rubikCube.rotateAngle=Math.toRadians(Temp_Rotation*5);
                        }
                        else if (rubikCube.mRotationPoint.RotationiAxis==2)	// 关于 z 轴旋转
                        {
                            RubikCube.Vec2 projection = new RubikCube.Vec2(moveVec.xx, moveVec.yy);
                            double Temp_Rotation = projection.GetMouldLength() / (rubikCube.sidelength_rc/9);
                            if (projection.xx < 0.0)Temp_Rotation = -Temp_Rotation;	// 如果是逆时针旋转
                            if (rubikCube.isRotationOpposite_Vertical())Temp_Rotation = -Temp_Rotation;	// 如果要颠倒
                            rubikCube.rotateAngle=Math.toRadians(Temp_Rotation*5);
                        }
                    }
                }
                break;
            }
            /**
             * 离开屏幕的位置
             */
            case MotionEvent.ACTION_UP: {
                if (rubikCube.mRotationPoint.RotationiAxis == 0) {
                    if(rubikCube.rotateAngle>Math.toRadians(45))
                    {
                        double begin=rubikCube.rotateAngle;
                        while(true)
                        {
                            rubikCube.rotateAngle+=(Math.toRadians(90)-begin)/20;
                            invalidate();
                            if(Math.abs(rubikCube.rotateAngle-Math.toRadians(90))<=
                                    Math.abs((Math.toRadians(90)-begin)/20))
                                break;
                            try {
                                Thread.sleep((long)50);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        rubikCube.rotateLayer_Across_Clockwise(rubikCube.mRotationPoint.RotateJudge);
                    }
                    else if(rubikCube.rotateAngle<-Math.toRadians(45))
                    {
                        double begin=rubikCube.rotateAngle;
                        while(true)
                        {
                            rubikCube.rotateAngle+=(Math.toRadians(0)-begin)/20;
                            invalidate();
                            if(Math.abs(rubikCube.rotateAngle-Math.toRadians(0))<=
                                    Math.abs((Math.toRadians(0)-begin)/20))
                                break;
                            try {
                                Thread.sleep((long)50);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        rubikCube.rotateLayer_Across_Counterclockwise(rubikCube.mRotationPoint.RotateJudge);
                    }
                }
                else if (rubikCube.mRotationPoint.RotationiAxis == 1) {
                    if(rubikCube.rotateAngle>Math.toRadians(45))
                    {
                        double begin=rubikCube.rotateAngle;
                        while(true)
                        {
                            rubikCube.rotateAngle+=(Math.toRadians(90)-begin)/20;
                            invalidate();
                            if(Math.abs(rubikCube.rotateAngle-Math.toRadians(90))<=
                                    Math.abs((Math.toRadians(90)-begin)/20))
                                break;
                            try {
                                Thread.sleep((long)50);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        rubikCube.rotateLayer_Straight_Clockwise(rubikCube.mRotationPoint.RotateJudge);
                    }
                    else if(rubikCube.rotateAngle<-Math.toRadians(45))
                    {
                        double begin=rubikCube.rotateAngle;
                        while(true)
                        {
                            rubikCube.rotateAngle+=(Math.toRadians(0)-begin)/20;
                            invalidate();
                            if(Math.abs(rubikCube.rotateAngle-Math.toRadians(0))<=
                                    Math.abs((Math.toRadians(0)-begin)/20))
                                break;
                            try {
                                Thread.sleep((long)50);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        rubikCube.rotateLayer_Straight_Counterclockwise(rubikCube.mRotationPoint.RotateJudge);
                    }
                }
                else if (rubikCube.mRotationPoint.RotationiAxis == 2) {
                    if(rubikCube.rotateAngle>Math.toRadians(45))
                    {
                        double begin=rubikCube.rotateAngle;
                        while(true)
                        {
                            rubikCube.rotateAngle+=(Math.toRadians(90)-begin)/20;
                            invalidate();
                            if(Math.abs(rubikCube.rotateAngle-Math.toRadians(90))<=
                                    Math.abs((Math.toRadians(90)-begin)/20))
                                break;
                            try {
                                Thread.sleep((long)50);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        rubikCube.rotateLayer_Vertical_Clockwise(rubikCube.mRotationPoint.RotateJudge);
                    }
                    else if(rubikCube.rotateAngle<-Math.toRadians(45))
                    {
                        double begin=rubikCube.rotateAngle;
                        while(true)
                        {
                            rubikCube.rotateAngle+=(Math.toRadians(0)-begin)/20;
                            invalidate();
                            if(Math.abs(rubikCube.rotateAngle-Math.toRadians(0))<=
                                    Math.abs((Math.toRadians(0)-begin)/20))
                                break;
                            try {
                                Thread.sleep((long)50);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        rubikCube.rotateLayer_Vertical_Counterclockwise(rubikCube.mRotationPoint.RotateJudge);
                    }
                }
                rubikCube.mRotationPoint.RotationiAxis = -1;
                rubikCube.mRotationPoint.RotateJudge = -1;
                rubikCube.mCubeIndex.index = -1;
                rubikCube.mCubeIndex.plane = -1;
                rubikCube.rotateAngle = 0;
                rubikCube.isEntirety = true;
                break;
            }
            default:
                break;
        }
        invalidate();
    }
}
