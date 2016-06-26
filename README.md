#Android-ProgressDialogDemo

**`ProgressDialog` 的正确用法是:**

1. **开发者自己不创建 `ProgressDialog`对象, 并且直接调用 `ProgressDialog` 类中的静态有参方法 `ProgressDialog.show(...)`**
---缺点是: 每次都要创建一个新的对象. 具体请看源码.

2. **开发者自己创建 `ProgressDialog`对象, 这时就禁止调用 `ProgressDialog` 类内部定义的有参的静态方法 `show(...)`, 而只能调用父类 `Dialog` 中无参的 `show()` 方法了.**
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

	--- 优点是: 一个页面只会创建一个 `ProgressDialog` 对象, 避免了频繁new对象.

**`ProgressDialog` 的错误用法是:**
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