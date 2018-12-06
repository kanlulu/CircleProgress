# CustomView
### 自定义View

让我们先从一个简单的例子入手：

一个简单的环形进度条，在进度条前端位置加一个白色小圆点，然后还有一个进度加载动画。

首先我们分析一下这个自定义View包含了以下几个部分：

- 圆环背景
- 圆环进度
- 白色小圆点
- 加载动画

大概就只有这几个部分组成。

**第一步：首先我们要新建一个自定义的类继承系统的View类：**

```java
public class CircleProgress extends View {
    private static final String TAG = "CircleProgress";

    public CircleProgress(Context context) {
        super(context);
        init();
    }

    public CircleProgress(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircleProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
 
    private void init(){
        //初始化操作
    }
}
```

需要注意的是：

1、其中还有一个构造方法

```java
public ProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
```

因为这个构造方法需要至少`API21`才能支持，所以我们不使用。

2、在每个构造方法中我们都需要调用`init()`方法来初始化我们的view。

3、除了上面代码中所展示的构造方法的写法，还有另一种方式：

```java
public class CircleProgress extends View {
    public CircleProgress(Context context) {
        this(context,null);
    }

    public CircleProgress(Context context, @Nullable AttributeSet attrs) {
        this(context,attrs,0);
    }

    public CircleProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        
    }

}
```

按照此种方式最终会调用到第三个构造方法，所以我们只需要在第三个构造方法里调用`init()`方法就可以了。

那么问题来了，现在有两种实现构造方法的方式，我们应该使用哪种？答案是：使用第一种方式更加稳妥。

使用第二种方式如果我们的自定义View(继承`TextView、ListView`等自定义View)拥有默认的`defStyleAttr`时候，就会被我们0所覆盖，这时候就会导致一系列问题。所以只有当我们能够完全确认自定义View没有默认的`defStyleAttr`的时候才可以使用第二种方式。

**第二步：初始化自定义View的数据**

```java
 private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
    }
```

对于我们这个需求来说，我们没有自定义参数，也没有其他需要初始化的东西。

如果我们需要自定义的参数：

- 创建自定义View之后，values文件夹下创建`attrs.xml`文件，在该文件中编写`styleable`和`item`等标签元素完成自定义属性的定义；
- 在布局文件中使用自定义属性；
- 在自定义View的构造方法中通过`TypedArray`获取。

*如果想要深入的了解自定义参数，请参考鸿洋的[这篇博客](https://blog.csdn.net/lmj623565791/article/details/45022631)。*

1.在`attrs`文件中创建自定义属性

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>

    <!--CircleProgress的自定义属性-->
    <declare-styleable name="CircleProgress">
        <attr name="progress" format="float" />
    </declare-styleable>

</resources>
```

2.在布局文件中使用自定义属性

```xml
    <com.kanlulu.customview.widget.CircleProgress
        android:layout_width="100dp"
        android:layout_height="100dp"
        android:layout_centerInParent="true"
        app:progress="75" />
```

3.在构造方法中获取属性值

```java
    private void init(Context context, @Nullable AttributeSet attrs) {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);

        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleProgress);
            creditProgress = typedArray.getFloat(R.styleable.CircleProgress_progress, 0);
            typedArray.recycle();
        }

    }
```

**第三步：重写`onDraw()`方法绘制不同部分**

```java
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //TODO getHeight()方法只有在onDraw的时候和之后的方法才有作用
        radius = (getHeight() - backCircleWith) / 2;//半径
        //绘制背景圆环
        paintBackCircle(canvas);
        //绘制进度
        paintProgress(canvas);
        //绘制圆点
        paintProgressPoint(canvas);
    }
```

*这里有一个需要注意的地方`getHeight()`方法获取的是我们控件的高度，它只有在`onDraw()`和之后的`onMeasure()`或`onlayout()`才有作用，如果我们在`init()`方法中获取`getHeight()`的结果是0。*

如上代码所示，我们分别新建三个方法完成这三部分的绘制：

1.绘制背景圆环

这是一个纯粹的圆环没有其他东西，我们只需要知道圆点和半径就可以了。

```java
    /**
     * 背景圆环
     *
     * @param canvas
     */
    private void paintBackCircle(Canvas canvas) {
        mPaint.setColor(getResources().getColor(R.color.circleBackColor));
        mPaint.setStrokeWidth(backCircleWith);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, radius, mPaint);
    }
```

2.绘制进度圆度圆环

其实就是绘制一个扇形圆环，对于绘制扇形而言，我们需要一个`RectF`对象来确定绘制的范围，然后我们需要一个扇形的起始位置的角度和扇形的角度，这样我们就可以以绘制出一个扇形了。

```java
    /**
     * 画进度扇形圆环
     *
     * @param canvas
     */
    private void paintProgress(Canvas canvas) {
        mPaint.setColor(getResources().getColor(R.color.circleProgressColor));
        mPaint.setStrokeWidth(backCircleWith);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        RectF rectF = new RectF(backCircleWith / 2, backCircleWith / 2, 2 * radius + backCircleWith / 2, 2 * radius + backCircleWith / 2);
        currentAngle += 5;
        if (currentAngle >= sweepAngle) currentAngle = sweepAngle;
        canvas.drawArc(rectF, 180, currentAngle, false, mPaint);
    }
```

`RectF`的构造方法有四个参数，分别是`left、top、right、bottom`，这四个位置限定了我们扇形绘制的范围，它在Android坐标体系中的位置如图所示：



Android坐标体系中关于角度位置的定义如图所示：



3.绘制进度前端的圆点

这个就是绘制一个点，关键要准确的确定点的坐标位置，这时候我们需要稍微用到一些简单的三角函数的知识，我们在园环中现在已知半径、角度和起始位置，我们就可以确定要绘制的点在圆环上的位置。

```java
    /**
     * 画进度前端的圆点
     *
     * @param canvas
     */
    private void paintProgressPoint(Canvas canvas) {
        mPaint.setColor(getResources().getColor(R.color.circlePoint));
        mPaint.setStrokeWidth(backCircleWith - 8);
        mPaint.setStyle(Paint.Style.STROKE);
        if (sweepAngle > 359 || sweepAngle <= 0) return;
	    //每次绘制递增5个数
        currentPointAngle += 5;
        if (currentPointAngle >= sweepAngle) currentPointAngle = sweepAngle;
        //绘制点的x坐标
        double x = getWidth() / 2 - radius * Math.cos(currentPointAngle * Math.PI / 180);
        //绘制点的y坐标
        double y = getHeight() / 2 - radius * Math.sin(currentPointAngle * Math.PI / 180);
        canvas.drawPoint((float) x, (float) y, mPaint);
    }
```

这些步骤完成后我们就可以看到静态的进度环了；现在我们需要加上渐进的动画。

在自定义View中没有直接的动画，要想实现动画效果我们只有通过重新绘制的方法来达到动画效果`invalidate()`;

我们可以每轮比上一次多绘制一点,

```java
//每次绘制递增5个数  
  currentAngle += 5;
  if (currentAngle >= sweepAngle) currentAngle = sweepAngle;
  canvas.drawArc(rectF, 180, currentAngle, false, mPaint);

//在onDraw()里重新绘制
 if (currentAngle != sweepAngle || currentPointAngle != sweepAngle) invalidate();//重新绘制
```

我们可以不延迟发起重新绘制，因为绘制一次本身就会耗费数十毫秒不等的时间。我们可以根据实际情况判断是否要设置延时的时间。
