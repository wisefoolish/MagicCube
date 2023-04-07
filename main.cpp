#include <iostream>
#include <graphics.h>
#include <math.h>
#define WIDTH 640						// 窗口宽度
#define HEIGHT 480						// 窗口高度
#define PI 3.14159265					// π
#define SIDE (min(WIDTH, HEIGHT) / 4)	// 正方体边长
#define GAMEPAD (SIDE / 2)				// 手柄，控制面旋转幅度的量
#define ZERO 0.1				// 对于浮点数来说的 0 值
#define PIECE 180						// 将一个 π 分为 PIECE 份
COLORREF DifferentColor = RGB(193, 181, 62);

// 旋转时是否相反
bool isOpposite(double Fi)
{
	int degree = Fi / PI * PIECE;
	int judge = abs(degree) % (2 * PIECE);
	if (judge > (PIECE / 2) && judge <= (PIECE / 2 * 3))
		return true;
	return false;
}

enum Plane
{
	Up, Down, Left, Right, Front, Back
};

// 六个面的颜色
COLORREF SurfaceColor[6] =
{
	RED, YELLOW, BLUE, GREEN, BROWN, MAGENTA
};

// 表示在正方体上的哪一面
struct CubeIndex
{
	Plane plane;
	int index;
};

// 判断旋转时是哪一层旋转
struct RotationPoint
{
	bool isAcrossAxis;
	bool isStraightAxis;
	bool isVerticalAxis;
	unsigned int Across_Judge;
	unsigned int Straight_Judge;
	unsigned int Vertical_Judge;
};

// 二维向量，也可以表示一个坐标
struct Vec2
{
	double x, y;
};
typedef struct Vec2;

Vec2 operator + (Vec2 a, Vec2 b)
{
	return { a.x + b.x, a.y + b.y };
}

Vec2 operator - (Vec2 a, Vec2 b)
{
	return { a.x - b.x, a.y - b.y };
}


// 得到向量缩短 num 倍后的向量
Vec2 operator / (Vec2 a, long double num)
{
	Vec2 result;
	result.x = a.x / num;
	result.y = a.y / num;
	return result;
}

// 得到向量延长 num 倍后的向量
Vec2 operator * (Vec2 a, long double num)
{
	Vec2 result;
	result.x = a.x * num;
	result.y = a.y * num;
	return result;
}

double operator * (Vec2 a, Vec2 b)
{
	return a.x * b.x + a.y * b.y;
}

// 三维向量，也可以表示一个坐标
struct Vec3
{
	double x, y, z;
};
typedef struct Vec3;

// 求两向量相减
Vec3 operator - (Vec3 a, Vec3 b)
{
	return { a.x - b.x, a.y - b.y, a.z - b.z };
}

// 求两向量相加
Vec3 operator + (Vec3 a, Vec3 b)
{
	return { a.x + b.x, a.y + b.y, a.z + b.z };
}

// 得到两向量点乘的值
double operator * (Vec3 a, Vec3 b)
{
	return a.x * b.x + a.y * b.y + a.z * b.z;
}

// 得到向量缩短 num 倍后的向量
Vec3 operator / (Vec3 a, long double num)
{
	Vec3 result;
	result.x = a.x / num;
	result.y = a.y / num;
	result.z = a.z / num;
	return result;
}

// 得到向量延长 num 倍后的向量
Vec3 operator * (Vec3 a, long double num)
{
	Vec3 result;
	result.x = a.x * num;
	result.y = a.y * num;
	result.z = a.z * num;
	return result;
}

// 得到一个向量的模长
double GetVec3Length(Vec3 vec)
{
	return sqrt(vec.x * vec.x + vec.y * vec.y + vec.z * vec.z);
}

double GetVec2Length(Vec2 vec)
{
	return sqrt(vec.x * vec.x + vec.y * vec.y);
}

// 得到向量 a 与向量 b 的夹角余弦值
double GetCosineOfTheAngle(Vec3 a, Vec3 b)
{
	return a * b / GetVec3Length(a) / GetVec3Length(b);
}

double GetCosineOfTheAngle(Vec2 a, Vec2 b)
{
	return a * b / GetVec2Length(a) / GetVec2Length(b);
}

// 得到向量 A 在向量 B 上的投影
Vec3 GetProjectionAOntoB(Vec3 A, Vec3 B)
{
	double num = GetCosineOfTheAngle(A, B);				// 得到向量 A，B 的夹角余弦值
	double length = GetVec3Length(A) * num;					// 向量 A 的模长乘 num 为向量 A 在向量 B 上投影的模长
	Vec3 result = B * (abs(length) / GetVec3Length(B));	// 向量 B 延长 length 倍再缩短 B 的模长倍就是向量 A 在向量 B 上的投影
	// 如果 length 比 0 小说明 num 小于 0，也就是两向量夹角大于 90 度，结果要变为相反向量
	if (length > 0)return result;
	return result * (-1.0);
}

// 根据投影面 x，y 轴正方向向量求出投影面法向量
Vec3 getVerticalAxis(Vec3 AuxiliaryVector[2])
{
	double x0 = AuxiliaryVector[0].x;
	double y0 = AuxiliaryVector[0].y;
	double z0 = AuxiliaryVector[0].z;
	double x1 = AuxiliaryVector[1].x;
	double y1 = AuxiliaryVector[1].y;
	double z1 = AuxiliaryVector[1].z;
	return { y0 * z1 - y1 * z0, x1 * z0 - x0 * z1, x0 * y1 - x1 * y0 };
}

// 将三维的点的值转换为在对应 xoy 面上的投影的坐标
typedef Vec3 DoubleVec3[2];
Vec2 Transform3DTo2D(Vec3 vertex, DoubleVec3 AuxiliaryVector)
{
	Vec2 result;
	Vec3 tempX = GetProjectionAOntoB(vertex, AuxiliaryVector[0]);	// 得到三维向量在 x 轴上的投影
	Vec3 tempY = GetProjectionAOntoB(vertex, AuxiliaryVector[1]);	// 得到三维向量在 y 轴上的投影
	result.x = GetVec3Length(tempX);								// 得到 tempX 的模长，模长就是结果的 x 值的绝对值
	result.y = GetVec3Length(tempY);								// 得到 tempY 的模长，模长就是结果的 y 值的绝对值
	if (tempX * AuxiliaryVector[0] < 0)result.x *= -1;				// 如果 tempX 向量与 x 轴正方向的向量夹角大于 90 度，也就是向量点乘为负数，那么结果的 x 值为负数
	if (tempY * AuxiliaryVector[1] < 0)result.y *= -1;				// 如果 tempY 向量与 y 轴正方向的向量夹角大于 90 度，也就是向量点乘为负数，那么结果的 y 值为负数
	// 以下为透视投影所做的操作，不需透视投影只需直接返回 result 值
	Vec3 Vec_Z = getVerticalAxis(AuxiliaryVector) * SIDE * 5;
	Vec3 target = vertex - Vec_Z;
	return result * (SIDE * 5 / GetVec3Length(GetProjectionAOntoB(target, Vec_Z)));
}

// 得到当前投影面的法向量，用于判断哪些面要显示出来
Vec3 getVerticalAxis(double Fi, double Th)
{
	return { cos(Fi) * sin(Th), -cos(Fi) * cos(Th), sin(Fi) };
}

void Line_Vec2(Vec2 a, Vec2 b)
{
	line(a.x, a.y, b.x, b.y);
}

// 画表面
void drawSurface(Vec3 Surface[4], Vec3 AuxiliaryVector[2], Vec2 pericenter_Sur, COLORREF color[9])
{
	setlinestyle(PS_SOLID, 2);
	Vec3 add_X = (Surface[1] - Surface[0]) / 3;
	Vec3 add_Y = (Surface[3] - Surface[0]) / 3;
	Vec3 add_All = add_X + add_Y;
	for (int i = 0; i < 3; i++)
	{
		for (int j = 0; j < 3; j++)
		{
			Vec3 position = Surface[0] + add_X * j + add_Y * i;
			// Vec2 pos_2d = Transform3DTo2D(position, AuxiliaryVector);
			setlinecolor(DifferentColor);
			Line_Vec2(Transform3DTo2D(position, AuxiliaryVector) + pericenter_Sur,
				Transform3DTo2D(position + add_X, AuxiliaryVector) + pericenter_Sur);
			Line_Vec2(Transform3DTo2D(position, AuxiliaryVector) + pericenter_Sur,
				Transform3DTo2D(position + add_Y, AuxiliaryVector) + pericenter_Sur);
			Line_Vec2(Transform3DTo2D(position + add_All, AuxiliaryVector) + pericenter_Sur,
				Transform3DTo2D(position + add_X, AuxiliaryVector) + pericenter_Sur);
			Line_Vec2(Transform3DTo2D(position + add_All, AuxiliaryVector) + pericenter_Sur,
				Transform3DTo2D(position + add_Y, AuxiliaryVector) + pericenter_Sur);
			Vec2 pericenter = Transform3DTo2D(position + add_All / 2, AuxiliaryVector) + pericenter_Sur;
			setfillcolor(color[i * 3 + j]);
			floodfill(pericenter.x, pericenter.y, DifferentColor);
			setlinecolor(WHITE);
			Line_Vec2(Transform3DTo2D(position, AuxiliaryVector) + pericenter_Sur,
				Transform3DTo2D(position + add_X, AuxiliaryVector) + pericenter_Sur);
			Line_Vec2(Transform3DTo2D(position, AuxiliaryVector) + pericenter_Sur,
				Transform3DTo2D(position + add_Y, AuxiliaryVector) + pericenter_Sur);
			Line_Vec2(Transform3DTo2D(position + add_All, AuxiliaryVector) + pericenter_Sur,
				Transform3DTo2D(position + add_X, AuxiliaryVector) + pericenter_Sur);
			Line_Vec2(Transform3DTo2D(position + add_All, AuxiliaryVector) + pericenter_Sur,
				Transform3DTo2D(position + add_Y, AuxiliaryVector) + pericenter_Sur);
		}
	}
}

// 画正方体
void drawCube(Vec3 Vertex[8], Vec3 AuxiliaryVector[2], Vec2 pericenter, COLORREF ColorArray[6][9])
{
	Vec2 Temp[8];
	Vec3 Vector_Z = getVerticalAxis(AuxiliaryVector);
	Vector_Z = Vector_Z / GetVec3Length(Vector_Z);
	Vec3 Surface[6][4] =
	{
		Vertex[1], Vertex[2], Vertex[6], Vertex[5],
		Vertex[0], Vertex[3], Vertex[7], Vertex[4],
		Vertex[3], Vertex[2], Vertex[6], Vertex[7],
		Vertex[0], Vertex[1], Vertex[5], Vertex[4],
		Vertex[4], Vertex[5], Vertex[6], Vertex[7],
		Vertex[0], Vertex[1], Vertex[2], Vertex[3],
	};
	if (Vector_Z.x > ZERO)drawSurface(Surface[0], AuxiliaryVector, pericenter, ColorArray[Right]);
	else if (Vector_Z.x < -ZERO)drawSurface(Surface[1], AuxiliaryVector, pericenter, ColorArray[Left]);
	if (Vector_Z.y > ZERO)drawSurface(Surface[2], AuxiliaryVector, pericenter, ColorArray[Back]);
	else if (Vector_Z.y < -ZERO)drawSurface(Surface[3], AuxiliaryVector, pericenter, ColorArray[Front]);
	if (Vector_Z.z > ZERO)drawSurface(Surface[4], AuxiliaryVector, pericenter, ColorArray[Up]);
	else if (Vector_Z.z < -ZERO)drawSurface(Surface[5], AuxiliaryVector, pericenter, ColorArray[Down]);
}

// 判断一个二维点是否在一个二维平面内
bool JudgePointInPlane(int x, int y, Vec3 Surface[4], Vec3 AuxiliaryVector[2], Vec2 pericenter)
{
	Vec2 Surface_Temp[4];
	for (int i = 0; i < 4; i++)Surface_Temp[i] = Transform3DTo2D(Surface[i], AuxiliaryVector) + pericenter;
	Vec2 vec_0 = Surface_Temp[1] - Surface_Temp[0];
	Vec2 vec_1 = Surface_Temp[3] - Surface_Temp[0];
	Vec2 vec_2 = { x - Surface_Temp[0].x, y - Surface_Temp[0].y };
	double standard = GetCosineOfTheAngle(vec_0, vec_1);
	if (GetCosineOfTheAngle(vec_2, vec_0) < standard || GetCosineOfTheAngle(vec_2, vec_1) < standard)
		return false;
	vec_0 = Surface_Temp[0] - Surface_Temp[1];
	vec_1 = Surface_Temp[2] - Surface_Temp[1];
	vec_2 = { x - Surface_Temp[1].x, y - Surface_Temp[1].y };
	standard = GetCosineOfTheAngle(vec_0, vec_1);
	if (GetCosineOfTheAngle(vec_2, vec_0) < standard || GetCosineOfTheAngle(vec_2, vec_1) < standard)
		return false;
	vec_0 = Surface_Temp[1] - Surface_Temp[2];
	vec_1 = Surface_Temp[3] - Surface_Temp[2];
	vec_2 = { x - Surface_Temp[2].x, y - Surface_Temp[2].y };
	standard = GetCosineOfTheAngle(vec_0, vec_1);
	if (GetCosineOfTheAngle(vec_2, vec_0) < standard || GetCosineOfTheAngle(vec_2, vec_1) < standard)
		return false;
	vec_0 = Surface_Temp[2] - Surface_Temp[3];
	vec_1 = Surface_Temp[0] - Surface_Temp[3];
	vec_2 = { x - Surface_Temp[3].x, y - Surface_Temp[3].y };
	standard = GetCosineOfTheAngle(vec_0, vec_1);
	if (GetCosineOfTheAngle(vec_2, vec_0) < standard || GetCosineOfTheAngle(vec_2, vec_1) < standard)
		return false;
	return true;
}

// 得到选中方块在二维面内的坐标
unsigned short GetSurfacePlace(Vec3 Surface[4], Vec3 AuxiliaryVector[2], Vec2 pericenter, int x, int y)
{
	short n = 0, m = 0;		// n 是 y 轴上的坐标，m 是 x 轴上的坐标
	Vec3 Surface_Temp[4];
	Vec3 add_X = (Surface[1] - Surface[0]) / 3;
	Vec3 add_Y = (Surface[3] - Surface[0]) / 3;
	for (n; n < 3; n++)
	{
		Surface_Temp[0] = Surface[0] + add_Y * n;
		Surface_Temp[1] = Surface[1] + add_Y * n;
		Surface_Temp[2] = Surface[1] + add_Y * (n + 1);
		Surface_Temp[3] = Surface[0] + add_Y * (n + 1);
		if (JudgePointInPlane(x, y, Surface_Temp, AuxiliaryVector, pericenter))break;
	}
	for (m; m < 3; m++)
	{
		Surface_Temp[0] = Surface[0] + add_X * m;
		Surface_Temp[1] = Surface[3] + add_X * m;
		Surface_Temp[2] = Surface[3] + add_X * (m + 1);
		Surface_Temp[3] = Surface[0] + add_X * (m + 1);
		if (JudgePointInPlane(x, y, Surface_Temp, AuxiliaryVector, pericenter))break;
	}
	return n * 3 + m;
}

// 得到点在魔方中哪一面的哪个位置
CubeIndex* getPlane(Vec3 Vertex[8], Vec3 AuxiliaryVector[2], Vec2 pericenter, int x, int y)
{
	CubeIndex* result = nullptr;
	Vec3 Vector_Z = getVerticalAxis(AuxiliaryVector);
	Vector_Z = Vector_Z / GetVec3Length(Vector_Z);
	Vec3 Surface[6][4] =
	{
		Vertex[1], Vertex[2], Vertex[6], Vertex[5],
		Vertex[0], Vertex[3], Vertex[7], Vertex[4],
		Vertex[3], Vertex[2], Vertex[6], Vertex[7],
		Vertex[0], Vertex[1], Vertex[5], Vertex[4],
		Vertex[4], Vertex[5], Vertex[6], Vertex[7],
		Vertex[0], Vertex[1], Vertex[2], Vertex[3],
	};
	if (Vector_Z.x > ZERO && JudgePointInPlane(x, y, Surface[0], AuxiliaryVector, pericenter))
	{
		result = new CubeIndex;
		result->plane = Right;
		result->index = GetSurfacePlace(Surface[0], AuxiliaryVector, pericenter, x, y);
	}
	else if (Vector_Z.x < -ZERO && JudgePointInPlane(x, y, Surface[1], AuxiliaryVector, pericenter))
	{
		result = new CubeIndex;
		result->plane = Left;
		result->index = GetSurfacePlace(Surface[1], AuxiliaryVector, pericenter, x, y);
	}
	else if (Vector_Z.y > ZERO && JudgePointInPlane(x, y, Surface[2], AuxiliaryVector, pericenter))
	{
		result = new CubeIndex;
		result->plane = Back;
		result->index = GetSurfacePlace(Surface[2], AuxiliaryVector, pericenter, x, y);
	}
	else if (Vector_Z.y < -ZERO && JudgePointInPlane(x, y, Surface[3], AuxiliaryVector, pericenter))
	{
		result = new CubeIndex;
		result->plane = Front;
		result->index = GetSurfacePlace(Surface[3], AuxiliaryVector, pericenter, x, y);
	}
	else if (Vector_Z.z > ZERO && JudgePointInPlane(x, y, Surface[4], AuxiliaryVector, pericenter))
	{
		result = new CubeIndex;
		result->plane = Up;
		result->index = GetSurfacePlace(Surface[4], AuxiliaryVector, pericenter, x, y);
	}
	else if (Vector_Z.z < -ZERO && JudgePointInPlane(x, y, Surface[5], AuxiliaryVector, pericenter))
	{
		result = new CubeIndex;
		result->plane = Down;
		result->index = GetSurfacePlace(Surface[5], AuxiliaryVector, pericenter, x, y);
	}
	return result;
}

// 画一条
void drawBar(Vec3 Surface[4], Vec3 AuxiliaryVector[2], Vec2 pericenter_Sur, COLORREF color[3], bool isAcross = true)
{
	setlinestyle(PS_SOLID, 2);
	if (isAcross)
	{
		Vec3 add_X = (Surface[1] - Surface[0]) / 3;
		Vec3 add_Y = (Surface[3] - Surface[0]);
		Vec3 add_All = add_X + add_Y;
		for (int i = 0; i < 3; i++)
		{
			Vec3 position = Surface[0] + add_X * i;
			setlinecolor(DifferentColor);
			Line_Vec2(Transform3DTo2D(position, AuxiliaryVector) + pericenter_Sur,
				Transform3DTo2D(position + add_X, AuxiliaryVector) + pericenter_Sur);
			Line_Vec2(Transform3DTo2D(position, AuxiliaryVector) + pericenter_Sur,
				Transform3DTo2D(position + add_Y, AuxiliaryVector) + pericenter_Sur);
			Line_Vec2(Transform3DTo2D(position + add_All, AuxiliaryVector) + pericenter_Sur,
				Transform3DTo2D(position + add_X, AuxiliaryVector) + pericenter_Sur);
			Line_Vec2(Transform3DTo2D(position + add_All, AuxiliaryVector) + pericenter_Sur,
				Transform3DTo2D(position + add_Y, AuxiliaryVector) + pericenter_Sur);
			Vec2 pericenter = Transform3DTo2D(position + add_All / 2, AuxiliaryVector) + pericenter_Sur;
			setfillcolor(color[i]);
			floodfill(pericenter.x, pericenter.y, DifferentColor);
			setlinecolor(WHITE);
			Line_Vec2(Transform3DTo2D(position, AuxiliaryVector) + pericenter_Sur,
				Transform3DTo2D(position + add_X, AuxiliaryVector) + pericenter_Sur);
			Line_Vec2(Transform3DTo2D(position, AuxiliaryVector) + pericenter_Sur,
				Transform3DTo2D(position + add_Y, AuxiliaryVector) + pericenter_Sur);
			Line_Vec2(Transform3DTo2D(position + add_All, AuxiliaryVector) + pericenter_Sur,
				Transform3DTo2D(position + add_X, AuxiliaryVector) + pericenter_Sur);
			Line_Vec2(Transform3DTo2D(position + add_All, AuxiliaryVector) + pericenter_Sur,
				Transform3DTo2D(position + add_Y, AuxiliaryVector) + pericenter_Sur);
		}
	}
	else
	{
		Vec3 add_X = (Surface[1] - Surface[0]);
		Vec3 add_Y = (Surface[3] - Surface[0]) / 3;
		Vec3 add_All = add_X + add_Y;
		for (int i = 0; i < 3; i++)
		{
			Vec3 position = Surface[0] + add_Y * i;
			setlinecolor(DifferentColor);
			Line_Vec2(Transform3DTo2D(position, AuxiliaryVector) + pericenter_Sur,
				Transform3DTo2D(position + add_X, AuxiliaryVector) + pericenter_Sur);
			Line_Vec2(Transform3DTo2D(position, AuxiliaryVector) + pericenter_Sur,
				Transform3DTo2D(position + add_Y, AuxiliaryVector) + pericenter_Sur);
			Line_Vec2(Transform3DTo2D(position + add_All, AuxiliaryVector) + pericenter_Sur,
				Transform3DTo2D(position + add_X, AuxiliaryVector) + pericenter_Sur);
			Line_Vec2(Transform3DTo2D(position + add_All, AuxiliaryVector) + pericenter_Sur,
				Transform3DTo2D(position + add_Y, AuxiliaryVector) + pericenter_Sur);
			Vec2 pericenter = Transform3DTo2D(position + add_All / 2, AuxiliaryVector) + pericenter_Sur;
			setfillcolor(color[i]);
			floodfill(pericenter.x, pericenter.y, DifferentColor);
			setlinecolor(WHITE);
			Line_Vec2(Transform3DTo2D(position, AuxiliaryVector) + pericenter_Sur,
				Transform3DTo2D(position + add_X, AuxiliaryVector) + pericenter_Sur);
			Line_Vec2(Transform3DTo2D(position, AuxiliaryVector) + pericenter_Sur,
				Transform3DTo2D(position + add_Y, AuxiliaryVector) + pericenter_Sur);
			Line_Vec2(Transform3DTo2D(position + add_All, AuxiliaryVector) + pericenter_Sur,
				Transform3DTo2D(position + add_X, AuxiliaryVector) + pericenter_Sur);
			Line_Vec2(Transform3DTo2D(position + add_All, AuxiliaryVector) + pericenter_Sur,
				Transform3DTo2D(position + add_Y, AuxiliaryVector) + pericenter_Sur);
		}
	}
}

// 涂一个全黑的面
void drawSurface(Vec3 Surface[4], Vec3 AuxiliaryVector[2], Vec2 pericenter_Sur)
{
	setlinecolor(DifferentColor);
	Line_Vec2(Transform3DTo2D(Surface[0], AuxiliaryVector) + pericenter_Sur,
		Transform3DTo2D(Surface[1], AuxiliaryVector) + pericenter_Sur);
	Line_Vec2(Transform3DTo2D(Surface[0], AuxiliaryVector) + pericenter_Sur,
		Transform3DTo2D(Surface[3], AuxiliaryVector) + pericenter_Sur);
	Line_Vec2(Transform3DTo2D(Surface[2], AuxiliaryVector) + pericenter_Sur,
		Transform3DTo2D(Surface[1], AuxiliaryVector) + pericenter_Sur);
	Line_Vec2(Transform3DTo2D(Surface[2], AuxiliaryVector) + pericenter_Sur,
		Transform3DTo2D(Surface[3], AuxiliaryVector) + pericenter_Sur);
	setfillcolor(BLACK);
	Vec2 pericenter = Transform3DTo2D((Surface[0] + Surface[2]) / 2, AuxiliaryVector) + pericenter_Sur;
	floodfill(pericenter.x, pericenter.y, DifferentColor);
	setlinecolor(WHITE);
	Line_Vec2(Transform3DTo2D(Surface[0], AuxiliaryVector) + pericenter_Sur,
		Transform3DTo2D(Surface[1], AuxiliaryVector) + pericenter_Sur);
	Line_Vec2(Transform3DTo2D(Surface[0], AuxiliaryVector) + pericenter_Sur,
		Transform3DTo2D(Surface[3], AuxiliaryVector) + pericenter_Sur);
	Line_Vec2(Transform3DTo2D(Surface[2], AuxiliaryVector) + pericenter_Sur,
		Transform3DTo2D(Surface[1], AuxiliaryVector) + pericenter_Sur);
	Line_Vec2(Transform3DTo2D(Surface[2], AuxiliaryVector) + pericenter_Sur,
		Transform3DTo2D(Surface[3], AuxiliaryVector) + pericenter_Sur);
}

// profile 是从 y 轴开始，顺时针转一圈
// 画以 x 轴为旋转轴的一层
void drawLayer_Across(Vec3 Vertex[8], Vec3 AuxiliaryVector[2], Vec2 pericenter,
	COLORREF profile[4][3], COLORREF* underSurface)
{
	Vec3 Vector_Z = getVerticalAxis(AuxiliaryVector);
	Vector_Z = Vector_Z / GetVec3Length(Vector_Z);
	Vec3 temp_1 = Vertex[7] - Vertex[0];
	Vec3 temp_2 = Vertex[4] - Vertex[3];
	Vec3 up_Vec = temp_1 + temp_2;
	Vec3 back_Vec = temp_1 - temp_2;
	if (Vector_Z * up_Vec > GetVec3Length(Vector_Z) * GetVec3Length(up_Vec) * ZERO)
	{
		Vec3 middle[4];
		middle[0] = Vertex[4];
		middle[1] = Vertex[5];
		middle[2] = Vertex[6];
		middle[3] = Vertex[7];
		drawBar(middle, AuxiliaryVector, pericenter, profile[0], false);
	}
	else if (Vector_Z * up_Vec < GetVec3Length(Vector_Z) * GetVec3Length(up_Vec) * -ZERO)
	{
		Vec3 middle[4];
		middle[0] = Vertex[0];
		middle[1] = Vertex[1];
		middle[2] = Vertex[2];
		middle[3] = Vertex[3];
		drawBar(middle, AuxiliaryVector, pericenter, profile[2], false);
	}

	if (Vector_Z * back_Vec > ZERO)
	{
		Vec3 middle[4];
		middle[0] = Vertex[3];
		middle[1] = Vertex[2];
		middle[2] = Vertex[6];
		middle[3] = Vertex[7];
		drawBar(middle, AuxiliaryVector, pericenter, profile[1], false);
	}
	else if (Vector_Z * back_Vec < -ZERO)
	{
		Vec3 middle[4];
		middle[0] = Vertex[0];
		middle[1] = Vertex[1];
		middle[2] = Vertex[5];
		middle[3] = Vertex[4];
		drawBar(middle, AuxiliaryVector, pericenter, profile[3], false);
	}
	Vec3 middle[4];
	if (Vector_Z.x > ZERO)
	{
		middle[0] = Vertex[1];
		middle[1] = Vertex[2];
		middle[2] = Vertex[6];
		middle[3] = Vertex[5];
	}
	else if (Vector_Z.x < -ZERO)
	{
		middle[0] = Vertex[0];
		middle[1] = Vertex[3];
		middle[2] = Vertex[7];
		middle[3] = Vertex[4];
	}
	if (underSurface == nullptr)
		drawSurface(middle, AuxiliaryVector, pericenter);
	else drawSurface(middle, AuxiliaryVector, pericenter, underSurface);
}

// 画以 y 轴为旋转轴的一层
void drawLayer_Straight(Vec3 Vertex[8], Vec3 AuxiliaryVector[2], Vec2 pericenter,
	COLORREF profile[4][3], COLORREF* underSurface)
{
	Vec3 Vector_Z = getVerticalAxis(AuxiliaryVector);
	Vector_Z = Vector_Z / GetVec3Length(Vector_Z);
	Vec3 temp_1 = Vertex[5] - Vertex[0];
	Vec3 temp_2 = Vertex[4] - Vertex[1];
	Vec3 up_Vec = temp_1 + temp_2;
	Vec3 right_Vec = temp_1 - temp_2;
	if (Vector_Z * up_Vec > ZERO)
	{
		Vec3 middle[4];
		middle[0] = Vertex[4];
		middle[1] = Vertex[5];
		middle[2] = Vertex[6];
		middle[3] = Vertex[7];
		drawBar(middle, AuxiliaryVector, pericenter, profile[0]);
	}
	else if (Vector_Z * up_Vec < -ZERO)
	{
		Vec3 middle[4];
		middle[0] = Vertex[0];
		middle[1] = Vertex[1];
		middle[2] = Vertex[2];
		middle[3] = Vertex[3];
		drawBar(middle, AuxiliaryVector, pericenter, profile[2]);
	}

	if (Vector_Z * right_Vec > ZERO)
	{
		Vec3 middle[4];
		middle[0] = Vertex[1];
		middle[1] = Vertex[2];
		middle[2] = Vertex[6];
		middle[3] = Vertex[5];
		drawBar(middle, AuxiliaryVector, pericenter, profile[1], false);
	}
	else if (Vector_Z * right_Vec < -ZERO)
	{
		Vec3 middle[4];
		middle[0] = Vertex[0];
		middle[1] = Vertex[3];
		middle[2] = Vertex[7];
		middle[3] = Vertex[4];
		drawBar(middle, AuxiliaryVector, pericenter, profile[3], false);
	}
	Vec3 middle[4];
	if (Vector_Z.y > ZERO)
	{
		middle[0] = Vertex[3];
		middle[1] = Vertex[2];
		middle[2] = Vertex[6];
		middle[3] = Vertex[7];
	}
	else if (Vector_Z.y < -ZERO)
	{
		middle[0] = Vertex[0];
		middle[1] = Vertex[1];
		middle[2] = Vertex[5];
		middle[3] = Vertex[4];
	}
	if (underSurface == nullptr)
		drawSurface(middle, AuxiliaryVector, pericenter);
	else drawSurface(middle, AuxiliaryVector, pericenter, underSurface);
}

// 画以 z 轴为旋转轴的一层
void drawLayer_Vertical(Vec3 Vertex[8], Vec3 AuxiliaryVector[2], Vec2 pericenter,
	COLORREF profile[4][3], COLORREF* underSurface)
{
	Vec3 Vector_Z = getVerticalAxis(AuxiliaryVector);
	Vector_Z = Vector_Z / GetVec3Length(Vector_Z);
	Vec3 temp_1 = Vertex[2] - Vertex[0];
	Vec3 temp_2 = Vertex[3] - Vertex[1];
	Vec3 back_Vec = temp_1 + temp_2;
	Vec3 right_Vec = temp_1 - temp_2;
	if (Vector_Z * back_Vec > ZERO)
	{
		Vec3 middle[4];
		middle[0] = Vertex[3];
		middle[1] = Vertex[2];
		middle[2] = Vertex[6];
		middle[3] = Vertex[7];
		drawBar(middle, AuxiliaryVector, pericenter, profile[0]);
	}
	else if (Vector_Z * back_Vec < -ZERO)
	{
		Vec3 middle[4];
		middle[0] = Vertex[0];
		middle[1] = Vertex[1];
		middle[2] = Vertex[5];
		middle[3] = Vertex[4];
		drawBar(middle, AuxiliaryVector, pericenter, profile[2]);
	}

	if (Vector_Z * right_Vec > ZERO)
	{
		Vec3 middle[4];
		middle[0] = Vertex[1];
		middle[1] = Vertex[2];
		middle[2] = Vertex[6];
		middle[3] = Vertex[5];
		drawBar(middle, AuxiliaryVector, pericenter, profile[1]);
	}
	else if (Vector_Z * right_Vec < -ZERO)
	{
		Vec3 middle[4];
		middle[0] = Vertex[0];
		middle[1] = Vertex[3];
		middle[2] = Vertex[7];
		middle[3] = Vertex[4];
		drawBar(middle, AuxiliaryVector, pericenter, profile[3]);
	}
	Vec3 middle[4];
	if (Vector_Z.z > ZERO)
	{
		middle[0] = Vertex[4];
		middle[1] = Vertex[5];
		middle[2] = Vertex[6];
		middle[3] = Vertex[7];
	}
	else if (Vector_Z.z < -ZERO)
	{
		middle[0] = Vertex[0];
		middle[1] = Vertex[1];
		middle[2] = Vertex[2];
		middle[3] = Vertex[3];
	}
	if (underSurface == nullptr)
		drawSurface(middle, AuxiliaryVector, pericenter);
	else drawSurface(middle, AuxiliaryVector, pericenter, underSurface);
}

// 画正在旋转中的魔方
void drawRotationCube(RotationPoint rotation, Vec3 Vertex[8], Vec3 AuxiliaryVector[2], Vec2 pericenter,
	COLORREF ColorArray[6][9], double Cos_A, double Sin_A)
{
	Vec3 Vector_Z = getVerticalAxis(AuxiliaryVector);
	Vector_Z = Vector_Z / GetVec3Length(Vector_Z);
	Vec3 add_Across = { SIDE / 3, 0, 0 };
	Vec3 add_Straight = { 0, SIDE / 3, 0 };
	Vec3 add_Vertical = { 0, 0, SIDE / 3 };
	Vec3 Temp_Vertex[3][8];
	if (rotation.isAcrossAxis)
	{
		int judge = rotation.Across_Judge;
		for (int i = 0; i < 3; i++)
		{
			Temp_Vertex[i][0] = Vertex[0] + add_Across * i;
			Temp_Vertex[i][1] = Vertex[0] + add_Across * (i + 1);
			Temp_Vertex[i][2] = Vertex[3] + add_Across * (i + 1);
			Temp_Vertex[i][3] = Vertex[3] + add_Across * i;

			Temp_Vertex[i][4] = Vertex[4] + add_Across * i;
			Temp_Vertex[i][5] = Vertex[4] + add_Across * (i + 1);
			Temp_Vertex[i][6] = Vertex[7] + add_Across * (i + 1);
			Temp_Vertex[i][7] = Vertex[7] + add_Across * i;
		}
		double r = SIDE / sqrt(2);
		for (int i = 0; i < 8; i++)
		{
			double Cos_P = Temp_Vertex[judge][i].y / r;
			double Sin_P = Temp_Vertex[judge][i].z / r;
			double Cos_Final = Cos_P * Cos_A + Sin_P * Sin_A;
			double Sin_Final = Sin_P * Cos_A - Sin_A * Cos_P;
			Temp_Vertex[judge][i].y = Cos_Final * r;
			Temp_Vertex[judge][i].z = Sin_Final * r;
		}
		if (Vector_Z.x > ZERO)
		{
			COLORREF* underSurface = new COLORREF[9];
			for (int i = 0; i < 9; i++)underSurface[i] = ColorArray[Right][i];
			for (int layer = 0; layer < 3; layer++)
			{
				COLORREF profile[4][3];
				for (int j = 0; j < 3; j++)profile[0][j] = ColorArray[Up][j * 3 + layer];
				for (int j = 0; j < 3; j++)profile[1][j] = ColorArray[Back][j * 3 + layer];
				for (int j = 0; j < 3; j++)profile[2][j] = ColorArray[Down][j * 3 + layer];
				for (int j = 0; j < 3; j++)profile[3][j] = ColorArray[Front][j * 3 + layer];
				if (layer == 2)
					drawLayer_Across(Temp_Vertex[layer], AuxiliaryVector, pericenter, profile, underSurface);
				else drawLayer_Across(Temp_Vertex[layer], AuxiliaryVector, pericenter, profile, nullptr);
			}
			delete[] underSurface;
		}
		else if (Vector_Z.x < ZERO)
		{
			COLORREF* underSurface = new COLORREF[9];
			for (int i = 0; i < 9; i++)underSurface[i] = ColorArray[Left][i];
			for (int layer = 2; layer >= 0; layer--)
			{
				COLORREF profile[4][3];
				for (int j = 0; j < 3; j++)profile[0][j] = ColorArray[Up][j * 3 + layer];
				for (int j = 0; j < 3; j++)profile[1][j] = ColorArray[Back][j * 3 + layer];
				for (int j = 0; j < 3; j++)profile[2][j] = ColorArray[Down][j * 3 + layer];
				for (int j = 0; j < 3; j++)profile[3][j] = ColorArray[Front][j * 3 + layer];
				if (layer == 0)
					drawLayer_Across(Temp_Vertex[layer], AuxiliaryVector, pericenter, profile, underSurface);
				else drawLayer_Across(Temp_Vertex[layer], AuxiliaryVector, pericenter, profile, nullptr);
			}
			delete[] underSurface;
		}
	}
	else if (rotation.isStraightAxis)
	{
		int judge = rotation.Straight_Judge;
		for (int i = 0; i < 3; i++)
		{
			Temp_Vertex[i][0] = Vertex[0] + add_Straight * i;
			Temp_Vertex[i][1] = Vertex[1] + add_Straight * i;
			Temp_Vertex[i][2] = Vertex[1] + add_Straight * (i + 1);
			Temp_Vertex[i][3] = Vertex[0] + add_Straight * (i + 1);

			Temp_Vertex[i][4] = Vertex[4] + add_Straight * i;
			Temp_Vertex[i][5] = Vertex[5] + add_Straight * i;
			Temp_Vertex[i][6] = Vertex[5] + add_Straight * (i + 1);
			Temp_Vertex[i][7] = Vertex[4] + add_Straight * (i + 1);
		}
		double r = SIDE / sqrt(2);
		for (int i = 0; i < 8; i++)
		{
			double Cos_P = Temp_Vertex[judge][i].x / r;
			double Sin_P = Temp_Vertex[judge][i].z / r;
			double Cos_Final = Cos_P * Cos_A + Sin_P * Sin_A;
			double Sin_Final = Sin_P * Cos_A - Sin_A * Cos_P;
			Temp_Vertex[judge][i].x = Cos_Final * r;
			Temp_Vertex[judge][i].z = Sin_Final * r;
		}
		if (Vector_Z.y > ZERO)
		{
			COLORREF* underSurface = new COLORREF[9];
			for (int i = 0; i < 9; i++)underSurface[i] = ColorArray[Back][i];
			for (int layer = 0; layer < 3; layer++)
			{
				COLORREF profile[4][3];
				for (int j = 0; j < 3; j++)profile[0][j] = ColorArray[Up][j + layer * 3];
				for (int j = 0; j < 3; j++)profile[1][j] = ColorArray[Right][j * 3 + layer];
				for (int j = 0; j < 3; j++)profile[2][j] = ColorArray[Down][j + layer * 3];
				for (int j = 0; j < 3; j++)profile[3][j] = ColorArray[Left][j * 3 + layer];
				if (layer == 2)
					drawLayer_Straight(Temp_Vertex[layer], AuxiliaryVector, pericenter, profile, underSurface);
				else drawLayer_Straight(Temp_Vertex[layer], AuxiliaryVector, pericenter, profile, nullptr);
			}
			delete[] underSurface;
		}
		else if (Vector_Z.y < ZERO)
		{
			COLORREF* underSurface = new COLORREF[9];
			for (int i = 0; i < 9; i++)underSurface[i] = ColorArray[Front][i];
			for (int layer = 2; layer >= 0; layer--)
			{
				COLORREF profile[4][3];
				for (int j = 0; j < 3; j++)profile[0][j] = ColorArray[Up][j + layer * 3];
				for (int j = 0; j < 3; j++)profile[1][j] = ColorArray[Right][j * 3 + layer];
				for (int j = 0; j < 3; j++)profile[2][j] = ColorArray[Down][j + layer * 3];
				for (int j = 0; j < 3; j++)profile[3][j] = ColorArray[Left][j * 3 + layer];
				if (layer == 0)
					drawLayer_Straight(Temp_Vertex[layer], AuxiliaryVector, pericenter, profile, underSurface);
				else drawLayer_Straight(Temp_Vertex[layer], AuxiliaryVector, pericenter, profile, nullptr);
			}
			delete[] underSurface;
		}
	}
	else if (rotation.isVerticalAxis)
	{
		int judge = rotation.Vertical_Judge;
		for (int i = 0; i < 3; i++)
		{
			Temp_Vertex[i][0] = Vertex[0] + add_Vertical * i;
			Temp_Vertex[i][1] = Vertex[1] + add_Vertical * i;
			Temp_Vertex[i][2] = Vertex[2] + add_Vertical * i;
			Temp_Vertex[i][3] = Vertex[3] + add_Vertical * i;

			Temp_Vertex[i][4] = Vertex[0] + add_Vertical * (i + 1);
			Temp_Vertex[i][5] = Vertex[1] + add_Vertical * (i + 1);
			Temp_Vertex[i][6] = Vertex[2] + add_Vertical * (i + 1);
			Temp_Vertex[i][7] = Vertex[3] + add_Vertical * (i + 1);
		}
		double r = SIDE / sqrt(2);
		for (int i = 0; i < 8; i++)
		{
			double Cos_P = Temp_Vertex[judge][i].x / r;
			double Sin_P = Temp_Vertex[judge][i].y / r;
			double Cos_Final = Cos_P * Cos_A + Sin_P * Sin_A;
			double Sin_Final = Sin_P * Cos_A - Sin_A * Cos_P;
			Temp_Vertex[judge][i].x = Cos_Final * r;
			Temp_Vertex[judge][i].y = Sin_Final * r;
		}
		if (Vector_Z.z > ZERO)
		{
			COLORREF* underSurface = new COLORREF[9];
			for (int i = 0; i < 9; i++)underSurface[i] = ColorArray[Up][i];
			for (int layer = 0; layer < 3; layer++)
			{
				COLORREF profile[4][3];
				for (int j = 0; j < 3; j++)profile[0][j] = ColorArray[Back][j + layer * 3];
				for (int j = 0; j < 3; j++)profile[1][j] = ColorArray[Right][j + layer * 3];
				for (int j = 0; j < 3; j++)profile[2][j] = ColorArray[Front][j + layer * 3];
				for (int j = 0; j < 3; j++)profile[3][j] = ColorArray[Left][j + layer * 3];
				if (layer == 2)
					drawLayer_Vertical(Temp_Vertex[layer], AuxiliaryVector, pericenter, profile, underSurface);
				else drawLayer_Vertical(Temp_Vertex[layer], AuxiliaryVector, pericenter, profile, nullptr);
			}
			delete[] underSurface;
		}
		else if (Vector_Z.z < ZERO)
		{
			COLORREF* underSurface = new COLORREF[9];
			for (int i = 0; i < 9; i++)underSurface[i] = ColorArray[Down][i];
			for (int layer = 2; layer >= 0; layer--)
			{
				COLORREF profile[4][3];
				for (int j = 0; j < 3; j++)profile[0][j] = ColorArray[Back][j + layer * 3];
				for (int j = 0; j < 3; j++)profile[1][j] = ColorArray[Right][j + layer * 3];
				for (int j = 0; j < 3; j++)profile[2][j] = ColorArray[Front][j + layer * 3];
				for (int j = 0; j < 3; j++)profile[3][j] = ColorArray[Left][j + layer * 3];
				if (layer == 0)
					drawLayer_Vertical(Temp_Vertex[layer], AuxiliaryVector, pericenter, profile, underSurface);
				else drawLayer_Vertical(Temp_Vertex[layer], AuxiliaryVector, pericenter, profile, nullptr);
			}
			delete[] underSurface;
		}
	}
}

// 一个平面的颜色数组旋转，isClockwise 是是否顺时针旋转的意思
void SurfaceRotation(COLORREF* ColorArray, bool isClockwise)
{
	int index_middle[3] = { 3, 7, 5 };
	int index_angle[3] = { 6, 8, 2 };
	if (isClockwise)
	{
		for (int i = 0; i < 3; i++)
		{
			COLORREF temp = ColorArray[index_middle[i]];
			ColorArray[index_middle[i]] = ColorArray[1];
			ColorArray[1] = temp;
			temp = ColorArray[index_angle[i]];
			ColorArray[index_angle[i]] = ColorArray[0];
			ColorArray[0] = temp;
		}
	}
	else
	{
		for (int i = 2; i >= 0; i--)
		{
			COLORREF temp = ColorArray[index_middle[i]];
			ColorArray[index_middle[i]] = ColorArray[1];
			ColorArray[1] = temp;
			temp = ColorArray[index_angle[i]];
			ColorArray[index_angle[i]] = ColorArray[0];
			ColorArray[0] = temp;
		}
	}
}

// 对于一个颜色数组进行首尾颠倒
void ReverseColorArray(COLORREF* array_color, int len)
{
	for (int i = 0; i < (len >> 1); i++)
	{
		COLORREF temp = array_color[i];
		array_color[i] = array_color[len - i - 1];
		array_color[len - i - 1] = temp;
	}
}

// 关于 x 轴旋转的某一层的颜色数组旋转
void rotateLayer_Across(COLORREF(&ColorArray)[6][9], int judge, bool isClockwise)
{
	COLORREF Temp[4][3];
	for (int i = 0; i < 3; i++)Temp[0][i] = ColorArray[Up][i * 3 + judge];
	for (int i = 0; i < 3; i++)Temp[1][i] = ColorArray[Back][i * 3 + judge];
	for (int i = 0; i < 3; i++)Temp[2][i] = ColorArray[Down][i * 3 + judge];
	for (int i = 0; i < 3; i++)Temp[3][i] = ColorArray[Front][i * 3 + judge];
	if (isClockwise)
	{
		ReverseColorArray(Temp[0], 3);
		ReverseColorArray(Temp[2], 3);
		for (int i = 0; i < 3; i++) ColorArray[Up][i * 3 + judge] = Temp[3][i];
		for (int i = 0; i < 3; i++)ColorArray[Back][i * 3 + judge] = Temp[0][i];
		for (int i = 0; i < 3; i++)ColorArray[Down][i * 3 + judge] = Temp[1][i];
		for (int i = 0; i < 3; i++)ColorArray[Front][i * 3 + judge] = Temp[2][i];
	}
	else
	{
		ReverseColorArray(Temp[1], 3);
		ReverseColorArray(Temp[3], 3);
		for (int i = 0; i < 3; i++) ColorArray[Up][i * 3 + judge] = Temp[1][i];
		for (int i = 0; i < 3; i++)ColorArray[Back][i * 3 + judge] = Temp[2][i];
		for (int i = 0; i < 3; i++)ColorArray[Down][i * 3 + judge] = Temp[3][i];
		for (int i = 0; i < 3; i++)ColorArray[Front][i * 3 + judge] = Temp[0][i];
	}
}

// 关于 y 轴旋转的某一层的颜色数组旋转
void rotateLayer_Straight(COLORREF(&ColorArray)[6][9], int judge, bool isClockwise)
{
	COLORREF Temp[4][3];
	for (int i = 0; i < 3; i++)Temp[0][i] = ColorArray[Up][i + judge * 3];
	for (int i = 0; i < 3; i++)Temp[1][i] = ColorArray[Right][i * 3 + judge];
	for (int i = 0; i < 3; i++)Temp[2][i] = ColorArray[Down][i + judge * 3];
	for (int i = 0; i < 3; i++)Temp[3][i] = ColorArray[Left][i * 3 + judge];
	if (isClockwise)
	{
		ReverseColorArray(Temp[0], 3);
		ReverseColorArray(Temp[2], 3);
		for (int i = 0; i < 3; i++) ColorArray[Up][i + judge * 3] = Temp[3][i];
		for (int i = 0; i < 3; i++)ColorArray[Right][i * 3 + judge] = Temp[0][i];
		for (int i = 0; i < 3; i++)ColorArray[Down][i + judge * 3] = Temp[1][i];
		for (int i = 0; i < 3; i++)ColorArray[Left][i * 3 + judge] = Temp[2][i];
	}
	else
	{
		ReverseColorArray(Temp[1], 3);
		ReverseColorArray(Temp[3], 3);
		for (int i = 0; i < 3; i++) ColorArray[Up][i + judge * 3] = Temp[1][i];
		for (int i = 0; i < 3; i++)ColorArray[Right][i * 3 + judge] = Temp[2][i];
		for (int i = 0; i < 3; i++)ColorArray[Down][i + judge * 3] = Temp[3][i];
		for (int i = 0; i < 3; i++)ColorArray[Left][i * 3 + judge] = Temp[0][i];
	}
}

// 关于 z 轴旋转的某一层的颜色数组旋转
void rotateLayer_Vertical(COLORREF(&ColorArray)[6][9], int judge, bool isClockwise)
{
	COLORREF Temp[4][3];
	for (int i = 0; i < 3; i++)Temp[0][i] = ColorArray[Back][i + judge * 3];
	for (int i = 0; i < 3; i++)Temp[1][i] = ColorArray[Right][i + judge * 3];
	for (int i = 0; i < 3; i++)Temp[2][i] = ColorArray[Front][i + judge * 3];
	for (int i = 0; i < 3; i++)Temp[3][i] = ColorArray[Left][i + judge * 3];
	if (isClockwise)
	{
		ReverseColorArray(Temp[0], 3);
		ReverseColorArray(Temp[2], 3);
		for (int i = 0; i < 3; i++) ColorArray[Back][i + judge * 3] = Temp[3][i];
		for (int i = 0; i < 3; i++)ColorArray[Right][i + judge * 3] = Temp[0][i];
		for (int i = 0; i < 3; i++)ColorArray[Front][i + judge * 3] = Temp[1][i];
		for (int i = 0; i < 3; i++)ColorArray[Left][i + judge * 3] = Temp[2][i];
	}
	else
	{
		ReverseColorArray(Temp[1], 3);
		ReverseColorArray(Temp[3], 3);
		for (int i = 0; i < 3; i++) ColorArray[Back][i + judge * 3] = Temp[1][i];
		for (int i = 0; i < 3; i++)ColorArray[Right][i + judge * 3] = Temp[2][i];
		for (int i = 0; i < 3; i++)ColorArray[Front][i + judge * 3] = Temp[3][i];
		for (int i = 0; i < 3; i++)ColorArray[Left][i + judge * 3] = Temp[0][i];
	}
}

// 初始化旋转中心点
void InitRotationPoint(RotationPoint* (&rotation), CubeIndex* originalPlace, Vec3 moveVec)
{
	int x = originalPlace->index % 3;
	int y = originalPlace->index / 3;
	switch (originalPlace->plane)
	{
	case Up:
	case Down:
		if (abs(moveVec.x) > abs(moveVec.y) && abs(moveVec.x) > GAMEPAD / 3)
		{
			rotation = new RotationPoint;
			rotation->isAcrossAxis = false;
			rotation->isStraightAxis = true;
			rotation->isVerticalAxis = false;
			rotation->Across_Judge = 0;
			rotation->Straight_Judge = y;
			rotation->Vertical_Judge = 0;
		}
		else if (abs(moveVec.x) < abs(moveVec.y) && abs(moveVec.y) > GAMEPAD / 3)
		{
			rotation = new RotationPoint;
			rotation->isAcrossAxis = true;
			rotation->isStraightAxis = false;
			rotation->isVerticalAxis = false;
			rotation->Across_Judge = x;
			rotation->Straight_Judge = 0;
			rotation->Vertical_Judge = 0;
		}
		break;
	case Left:
	case Right:
		if (abs(moveVec.y) > abs(moveVec.z) && abs(moveVec.y) > GAMEPAD / 3)
		{
			rotation = new RotationPoint;
			rotation->isAcrossAxis = false;
			rotation->isStraightAxis = false;
			rotation->isVerticalAxis = true;
			rotation->Across_Judge = 0;
			rotation->Straight_Judge = 0;
			rotation->Vertical_Judge = y;
		}
		else if (abs(moveVec.y) < abs(moveVec.z) && abs(moveVec.z) > GAMEPAD / 3)
		{
			rotation = new RotationPoint;
			rotation->isAcrossAxis = false;
			rotation->isStraightAxis = true;
			rotation->isVerticalAxis = false;
			rotation->Across_Judge = 0;
			rotation->Straight_Judge = x;
			rotation->Vertical_Judge = 0;
		}
		break;
	case Front:
	case Back:
		if (abs(moveVec.x) > abs(moveVec.z) && abs(moveVec.x) > GAMEPAD / 3)
		{
			rotation = new RotationPoint;
			rotation->isAcrossAxis = false;
			rotation->isStraightAxis = false;
			rotation->isVerticalAxis = true;
			rotation->Across_Judge = 0;
			rotation->Straight_Judge = 0;
			rotation->Vertical_Judge = y;
		}
		else if (abs(moveVec.x) < abs(moveVec.z) && abs(moveVec.z) > GAMEPAD / 3)
		{
			rotation = new RotationPoint;
			rotation->isAcrossAxis = true;
			rotation->isStraightAxis = false;
			rotation->isVerticalAxis = false;
			rotation->Across_Judge = x;
			rotation->Straight_Judge = 0;
			rotation->Vertical_Judge = 0;
		}
		break;
	default:
		break;
	}
}
// x 轴固定在 xoy 平面上，旋转 x 轴和 z 轴就能看到这个三维物体的所有角度！！！
// 右键拖动是拧动一个魔方的操作
// 右键点击后判断中心点到点击的地方的向量，判断要精确到一个小小的平面
int main()
{
	initgraph(WIDTH, HEIGHT);
	BeginBatchDraw();
	Vec3 AuxiliaryVector[2] = { { 1, 0, 0 }, { 0, 1, 0 } };	// 辅助向量，分别是 x 轴，y 轴的单位向量
	Vec3 Vertex[8];										// 8 个顶点的坐标
	Vertex[0] = { -GAMEPAD, -GAMEPAD, -GAMEPAD };
	Vertex[1] = { GAMEPAD, -GAMEPAD, -GAMEPAD };
	Vertex[2] = { GAMEPAD, GAMEPAD, -GAMEPAD };
	Vertex[3] = { -GAMEPAD, GAMEPAD, -GAMEPAD };
	Vertex[4] = { -GAMEPAD, -GAMEPAD, GAMEPAD };
	Vertex[5] = { GAMEPAD, -GAMEPAD, GAMEPAD };
	Vertex[6] = { GAMEPAD, GAMEPAD, GAMEPAD };
	Vertex[7] = { -GAMEPAD, GAMEPAD, GAMEPAD };
	COLORREF SurfaceNightColor[6][9];
	for (int i = 0; i < 6; i++)
		for (int j = 0; j < 9; j++)
			SurfaceNightColor[i][j] = SurfaceColor[i];
	ExMessage msg;								// 鼠标信息
	bool ispress = false;						// 是否按下
	bool isRpress = false, isLpress = false;	// 左右键是否按下
	double originalX = 0, originalY = 0;		// 原来的坐标
	CubeIndex* originalPlace = nullptr;			// 右键点击时鼠标所在魔方的位置
	RotationPoint* rotation = nullptr;			// 旋转魔方时旋转的中心点
	double Angle_Rotation = 0;					// 旋转魔方时旋转的度数
	bool isRotationOpposite_Across = false;		// 关于 x 轴旋转时是否要颠倒
	bool isRotationOpposite_Straight = false;	// 关于 y 轴旋转时是否要颠倒
	bool isRotationOpposite_Vertical = false;	// 关于 z 轴旋转时是否要颠倒
	drawCube(Vertex, AuxiliaryVector, { WIDTH / 2.0, HEIGHT / 2.0 }, SurfaceNightColor);
	FlushBatchDraw();
	bool isExit = false;
	while (!isExit)
	{
		if (peekmessage(&msg, EM_MOUSE | EM_KEY))
		{
			if (msg.message == WM_KEYDOWN)
			{
				switch (msg.vkcode)
				{
				case VK_RETURN:
					isExit = true;
					break;
				}
			}
			else
			{
				if (!ispress && (msg.lbutton || msg.rbutton))
				{
					ispress = true;
					if (msg.rbutton)
					{
						isRpress = true;
						Vec3 Vector_Z = getVerticalAxis(AuxiliaryVector);
						originalPlace = getPlane(Vertex, AuxiliaryVector, { WIDTH / 2, HEIGHT / 2 },
							msg.x, msg.y);
						Angle_Rotation = 0;
						if (Vector_Z.y < -ZERO)isRotationOpposite_Vertical = true;
						if (Vector_Z.z < -ZERO)
						{
							isRotationOpposite_Across = true;
							isRotationOpposite_Straight = true;
						}
					}
					else isLpress = true;
					originalX = msg.x;
					originalY = msg.y;
				}
				else if (isLpress && msg.lbutton)
				{
					double DeltaFi = (msg.y - originalY) / 6 / GAMEPAD * PI;
					double DeltaTh = (msg.x - originalX) / GAMEPAD / 6 * PI;
					cleardevice();
					Vec3 temp_X = AuxiliaryVector[0], temp_Y = AuxiliaryVector[1],
						temp_Z = getVerticalAxis(AuxiliaryVector);
					AuxiliaryVector[0] = temp_X * cos(DeltaTh) + temp_Z * sin(DeltaTh);
					temp_Z = temp_Z * cos(DeltaTh) - temp_X * sin(DeltaTh);
					AuxiliaryVector[1] = temp_Y * cos(DeltaFi) + temp_Z * sin(DeltaFi);
					drawCube(Vertex, AuxiliaryVector, { WIDTH / 2.0, HEIGHT / 2.0 },
						SurfaceNightColor);
					FlushBatchDraw();
					originalX = msg.x;
					originalY = msg.y;
				}
				else if (isRpress && msg.rbutton)
				{
					// 在三维空间内移动的向量
					Vec3 moveVec = AuxiliaryVector[0] * (msg.x - originalX) + AuxiliaryVector[1] * (msg.y - originalY);
					if (rotation != nullptr)
					{
						cleardevice();
						if (rotation->isAcrossAxis)	// 关于 x 轴旋转
						{
							Vec2 projection = { moveVec.y, moveVec.z };
							double Temp_Rotation = GetVec2Length(projection) / 6 / GAMEPAD * PI;
							if (projection.x < -ZERO)Temp_Rotation = -Temp_Rotation;	// 如果是逆时针旋转
							if (isRotationOpposite_Across)Angle_Rotation -= Temp_Rotation;	// 如果要颠倒
							else Angle_Rotation += Temp_Rotation;
						}
						else if (rotation->isStraightAxis)	// 关于 y 轴旋转
						{
							Vec2 projection = { moveVec.x, moveVec.z };
							double Temp_Rotation = GetVec2Length(projection) / 6 / GAMEPAD * PI;
							if (projection.x < -ZERO)Temp_Rotation = -Temp_Rotation;	// 如果是逆时针旋转
							if (isRotationOpposite_Straight)Angle_Rotation -= Temp_Rotation;	// 如果要颠倒
							else Angle_Rotation += Temp_Rotation;
						}
						else if (rotation->isVerticalAxis)	// 关于 z 轴旋转
						{
							Vec2 projection = { moveVec.x, moveVec.y };
							double Temp_Rotation = GetVec2Length(projection) / 6 / GAMEPAD * PI;
							if (projection.x < -ZERO)Temp_Rotation = -Temp_Rotation;
							if (isRotationOpposite_Vertical)Angle_Rotation -= Temp_Rotation;
							else Angle_Rotation += Temp_Rotation;
						}
						drawRotationCube(*rotation, Vertex, AuxiliaryVector, { WIDTH / 2, HEIGHT / 2 },
							SurfaceNightColor, cos(Angle_Rotation), sin(Angle_Rotation));
						FlushBatchDraw();
						originalX = msg.x;
						originalY = msg.y;
					}
					else if (originalPlace != nullptr)InitRotationPoint(rotation, originalPlace, moveVec);
				}
				else if (ispress && !(msg.lbutton || msg.rbutton))
				{
					ispress = false;
					isRpress = false;
					isLpress = false;
					if (rotation != nullptr)
					{
						// 要改的只有颜色的位置，点的位置始终没变
						// 根据旋转的点和旋转的角度来决定改变哪些颜色的位置
						// 转过 45 度以上的时候就会变
						if (abs(Angle_Rotation) > PI / 4)
						{
							bool isClockwise = Angle_Rotation > 0;
							if (rotation->isAcrossAxis)
							{
								int judge = rotation->Across_Judge;
								rotateLayer_Across(SurfaceNightColor, judge, isClockwise);
								if (judge == 0)SurfaceRotation(SurfaceNightColor[Left], isClockwise);
								else if (judge == 2)SurfaceRotation(SurfaceNightColor[Right], isClockwise);
							}
							else if (rotation->isStraightAxis)
							{
								int judge = rotation->Straight_Judge;
								rotateLayer_Straight(SurfaceNightColor, judge, isClockwise);
								if (judge == 0)SurfaceRotation(SurfaceNightColor[Front], isClockwise);
								else if (judge == 2)SurfaceRotation(SurfaceNightColor[Back], isClockwise);
							}
							else if (rotation->isVerticalAxis)
							{
								int judge = rotation->Vertical_Judge;
								rotateLayer_Vertical(SurfaceNightColor, judge, isClockwise);
								if (judge == 0)SurfaceRotation(SurfaceNightColor[Down], isClockwise);
								else if (judge == 2)SurfaceRotation(SurfaceNightColor[Up], isClockwise);
							}
						}
						cleardevice();
						drawCube(Vertex, AuxiliaryVector, { WIDTH / 2.0, HEIGHT / 2.0 },
							SurfaceNightColor);
						FlushBatchDraw();
						delete rotation;
						rotation = nullptr;
					}
					if (originalPlace != nullptr)
					{
						isRotationOpposite_Across = false;
						isRotationOpposite_Straight = false;
						isRotationOpposite_Vertical = false;
						delete originalPlace;
						originalPlace = nullptr;
					}
				}
			}
		}
	}
	closegraph();
	return 0;
}
