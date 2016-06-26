#Android-ProgressDialog 使用时的注意事项

### `ProgressDialog` 的正确用法
有以下几种用法:   
1. <b>开发者自己不创建 `ProgressDialog` 对象, 并且直接调用 `ProgressDialog` 类中的静态有参方法 `ProgressDialog.show(...)`, 但一定要存储该 `show()` 方法返回的`ProgressDialog`对象, 在需要dismiss该对话框的地方, 就调用这个返回的对象的 `dismiss()`方法</b>. 
代码结构如下:
```java
ProgressDialog mProgressDialog = ProgressDialog.show(MainActivity.this, null, "加载中...", true, true, new DialogInterface.OnCancelListener() {
	@Override
	public void onCancel(DialogInterface dialog) {
	    // TODO: 取消网络请求
	}
});

...

mProgressDialog.dismiss();
```
------缺点是: 每次都要创建一个新的 `ProgressDialog` 对象. 具体原因请看源码.   
2. **开发者自己创建 `ProgressDialog`对象, 这时就禁止调用 `ProgressDialog` 类内部定义的有参的静态方法 `show(...)`, 而只能调用父类 `Dialog` 中定义的无参 `show()` 方法.**
通常代码如下:
```java
private ProgressDialog mProgressDialog;

if (mProgressDialog == null) {
    mProgressDialog = new ProgressDialog(XXXActivity.this);
    mProgressDialog.setMessage("加载中...");
    mProgressDialog.setCancelable(true);
    mProgressDialog.setCanceledOnTouchOutside(false);
    mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
            LoadMoreTask.this.cancel(false);
        }
    });
}
mProgressDialog.show();
```

------ 优点是: 一个页面只会创建一个 `ProgressDialog` 对象, 避免了频繁new对象.

###`ProgressDialog` 的错误用法
错误用法是: 先通过 new 的方式创建一个 `ProgressDialog` 对象, 例如叫做: `mProgressDialog`, 然后调用该对象的静态有参 `show(...)` 方法, 即: 调用类似于这样的代码: `mProgressDialog.show(context, null, "加载中...", true, true, null)`. 而调用这句代码后, 界面上立刻显示出了一个对话框, 这些操作看上去都很顺利, 那么代码应该都是正确的吧, 但是在需要让当前界面上显示的对话框消失时, 你毫不犹豫地调用 `mProgressDialog.dismiss()` 方法, 却发现对话框并没有取消. 典型的错误代码结构如下:

```java
private ProgressDialog mProgressDialog;

if (mProgressDialog == null) {
    mProgressDialog = new ProgressDialog(XXXActivity.this);
    mProgressDialog.setCanceledOnTouchOutside(false);
}
mProgressDialog.show(XXXActivity.this, null, "加载中...", true, true, new DialogInterface.OnCancelListener() {
    @Override
    public void onCancel(DialogInterface dialog) {
       // TODO: 取消网络请求
    }
});
```
具体原因, 只能通过断点调试, 结合查看 `ProgressDialog` 类中静态有参 `show(...)` 方法的源码来进行分析, 该方法的源码如下:
```java
public static ProgressDialog show(Context context, CharSequence title,
    CharSequence message, boolean indeterminate,
    boolean cancelable, OnCancelListener cancelListener) {
	ProgressDialog dialog = new ProgressDialog(context);
	dialog.setTitle(title);
	dialog.setMessage(message);
	dialog.setIndeterminate(indeterminate);
	dialog.setCancelable(cancelable);
	dialog.setOnCancelListener(cancelListener);
	dialog.show();
	return dialog;
}
```
查看上述源码, 会发现该方法其实在其内部创建了一个 `ProgressDialog` 对象 `dialog`, 并让这个 `dialog` 显示出来, 也就是说, 当前显示的对话框其实是 `dialog`, 而不是 `mProgressDialog`, 所以我们调用 `mProgressDialog` 的 `dismiss()` 方法不起作用也就不足为奇了. 再观察该方法的返回值, 会发现, 该方法会将创建出来的 `dialog` 作为返回值, 所以要使用该静态方法, 我们就必须要定义一个引用来指向该方法内部创建出来的 `dialog` 对象, 然后需要让对话框消失时, 就调用我们定义的这个引用的 `dismiss()` 方法才能让对话框成功消失. 由于每次调用该静态方法都会创建一个 `ProgressDialog` 对象, 对内存是种浪费. 所以如果要调用 `ProgressDialog` 类中定义的静态有参 `show(...)` 方法, 要求用法正确并且一个页面最多只能创建一个 `ProgressDialog` 对象, 那么只能使用如下略显怪异的用法:
```java
private ProgressDialog mProgressDialog;
if (mProgressDialog == null) {
	mProgressDialog = ProgressDialog.show(MainActivity.this, null, "加载中...", true, true, new DialogInterface.OnCancelListener() {
	    @Override
	    public void onCancel(DialogInterface dialog) {
	        // TODO: 取消网络请求
	    }
	});
}
if (!mProgressDialog.isShowing()) {
	mProgressDialog.show();
}
```

###结论
通过上述分析, 我们可以得出结论:   
**`ProgressDialog` 类中的静态有参方法 `show(...)` 是一种不太好的设计, 应尽可能避免使用.**   
原因如下:   

1. 如果不像上述代码那样做特殊处理, 那么每次调用该方法都会创建一个 `ProgressDialog` 对象, 造成内存资源浪费.
2. 容易写出错误代码. 由于对象也可以调用该对象所属类的静态方法, 所以一旦开发者自己通过 new 的方式创建一个 `ProgressDialog` 对象, 然后对该对象调用了静态有参方法 `show(...)`, 那么如果不分析源码, 开发者非常容易误以为当前界面上显示出来的就是他通过new的方式创建出来的那个 `ProgressDialog` 对象, 直到调用该对象的 `dismiss()` 方法后发现对话框无法取消, 分析源码后才能发现错误原因.
