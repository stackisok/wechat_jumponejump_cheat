一个用JAVA实现的微信跳一跳辅助
===
使用方法
-----
1.安装ADB工具。(若已安装请忽略，若未安装，windows平台可以使用adbTool目录下的platform-tools-latest-windows.zip)   

2.安装[opencv](http://opencv.org/opencv-3-2.html)。  

3.连接手机，打开USB调试。  

4.打开游戏到跳方块的页面，打开程序。  



大概原理介绍
-----
1.将手机跳一跳的屏幕截屏后传到电脑。  

2.java读取截屏，分析出棋子的坐标和目标点的坐标。  

3.根据坐标，计算按压的时间。  

4.通过ADB模拟手指按压，实现辅助功能。  


详细原理分析
-----
首先，这个辅助用到了opencv的库，这是一个很多人贡献的跨平台计算机视觉库，具体可以百度了解。 
我们看一张跳一跳的图片   

![](http://img.blog.csdn.net/20180129125155669?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvbml1bmFpMTEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
  
  这个游戏逻辑很简单，就是从一个方格跳到另一个方格上，按压时间的长短控制跳动的距离，所以辅助的逻辑也很简单，就是计算出棋子到下一个方格的距离是多少，然后通过距离来计算按压时间。 
这里难度最大的是如何计算出棋子到下一个方格的距离是多少。那我是这么来做的，首先opencv库里有一个函数叫matchTemplate，它能从图中匹配出你预设的模板的图片的起始位置。这里，我的模板是这张图片，然后这个匹配度几乎是100%的。    

![](http://img.blog.csdn.net/20180129125706201?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvbml1bmFpMTEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
   
   
![](http://img.blog.csdn.net/20180129130443351?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvbml1bmFpMTEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
  
  当我们得到棋子的起始位置的时候，通过起始位置的y坐标+棋子高度得到实际棋子的y坐标，起始位置的x坐标+棋子宽度得到实际棋子的x坐标。

之后我们要计算的是落脚点的坐标。这里我用的方法是opencv的边缘检测。因为图片的方块边缘清晰，所以用边缘检测可以得到非常清晰的方块边缘，像下面的图片， 
  
  
![](http://img.blog.csdn.net/20180129131508890?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvbml1bmFpMTEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast) 
  
  有了这张图后，你就能做很多事情了，我的做法是从红线开始往下扫，但第一次找到像素点为白色的时候往下找，但在此遇到白色像素的点时，计算两个点的中间点的位置。像下图这样   

![](http://img.blog.csdn.net/20180129132321325?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvbml1bmFpMTEy/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
   
   当知道这2个点时就可以计算出距离了。然后就可以通过ADB进行模拟跳跃了。这里的逻辑大部分的方格都能跳跃了，但是个别的方格会出问题，后面会对找落脚点的逻辑进行更细的分解，实现绝大部分的方格都能跳过。 
