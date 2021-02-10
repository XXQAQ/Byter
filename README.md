# Byter
字节对象转换框架，一个基于字节的 Gson/FastJson

众所周知，Json数据的序列化内容就是字符串，如果说Gson/FastJson是基于字符串的Json转化框架，那么Byter就是基于字节流的Json转化框架。
设计Byter之处，就是为了快速且优雅地解析一些底层协议，如蓝牙、TCP/IP、MQTT等，均需要通过解析字节流来转换数据。在正常的开发流程中，往往都是采用写死下标的方法来处理字节数据，这种处理一般情况下还好，如若一旦涉及到协议增删改，那就非常头疼了。要是再多改上几次，最好再改改数据结构，恐怕就会怀疑自己是否适合从事开发行业了。

为了让字节数据解析像Gson/FastJson那样简单粗暴，因此我设计了Byter。而Byter的使用相当简便，只需要掌握三步骤即可：

第一步：按照协议定义你的对象属性字段；
需要注意的是，字段的数据类型必须与你的协议一一对应。如：协议定义了一个 2字节大小的head，那么在对象中定义的head也应该是一个short而不能定义为int。因为java标准中int占4个字节，长度不相同当然不能混定义，不太清楚各个基础数据类型占用的字节大小的话可以自行搜索一下。
```
    class InfoData{
        short head;
        byte cmd;
        short version;
        short extra;
        byte sum;
    }
```

第二步：标注字段顺序
按照协议的字段顺序，在你的属性字段上使用@Order的注解描述你的协议顺序。
需要注意的是，字段的标注顺序并不一定要求1、2、3、4、5这样依次递增，只要满足大小规律即可，如1，10，20，21，22，25，100。这样可以预留一些余地让子类来定义。
```
    class InfoData{
        @Order(order = 1)
        short head;
        @Order(order = 2)
        byte cmd;
        @Order(order = 10)
        short version;
        @Order(order = 11)
        short extra;
        @Order(order = 20)
        byte sum;
    }
```

第三步：
使用Byter的 toBytes 或者 fromBytes ，体验数据与对象的一键转化。
```
    InfoData infoData = Byter.fromBytes(InfoData.class,bytes);
```

有些朋友可能会问了：完了，就这？？当然，基础的使用方式仅此而已，但真实的情况肯定不会这么简单。在我们的实际应用中，一个对象里可能还会嵌套N级对象；并且有些数据还是数组，而对于字节协议来说数组的学问就多了，有些协议的数组定长，有些协议的数组需要根据某个字段的值来决定，还有一些协议既不定长也不需要定义，这么多需求都能满足？

对于以上情况，都是框架者需要考虑的问题，Byter不一定能保证全部满足需求，不过对于正常流程的需求Byter全部都考虑到了。对于一些不太常见但是确实存在的协议，也可以与我私聊，Byter会通过迭代尽力满足大家。同时Byter的源码也是非常精简的，一共2个类+2个注解，可以随时拷贝走自己改改源码。

首先对于第一个问题：对象里面可能会嵌套N层对象——这个完全不需要担心，Byter使用递归的方式处理了任意层级的对象数据，并且支持多层继承的关系，你只需要担心自己的对象是否按照协议定义。
```
    class Parent {
        @Order(order = 1)
        short head;
        @Order(order = 2)
        byte cmd;
        @Order(order = 100)
        byte sum;
    }

    class Child extends Parent{
        @Order(order = 10)
        Data1 data1;
        @Order(order = 11)
        Data2 data2;
    }

    class Data1{
        @Order(order = 1)
        int x;
        @Order(order = 2)
        long y;
        @Order(order = 3)
        short z;
    }

    class Data2{
        @Order(order = 1)
        double a;
        @Order(order = 2)
        float b;
    }
```
其次第二个问题，对于数组类型的数据来说Byter也有一套解决方案：通过在数组字段上标注@Length的方式来描述数组的长度。
@Length有两个方法，分别是 length 和 lengthByField。如果你的数组长度固定，那么使用 length = n 的方式来定义，如果是依赖某个字段的值的话，那就使用lengthByField = “fieldName”来定义。如果数组既不定长，也不依赖某个字段，那么就不需要用这个注解。Byter会计算后面的字段长度，再根据总长，从而推导出当前的数组长度。当然，这样就必须要求你的协议中不能同时存在两个以上的数组，否则无法推导。
还有一点需要说明：数组既可以是基础数据类型，也可以是对象。
长度依赖某个字段：
```
    class StateArray{
        @Order(order = 1)
        short head;
        @Order(order = 2)
        byte cmd;
        @Order(order = 10)
        byte stateCount;
        @Length(lengthByField = "stateCount")
        @Order(order = 11)
        State[] states;
        @Order(order = 100)
        byte sum;
    }

    class State {
        @Order(order = 1)
        byte key;
        @Order(order = 2)
        short value;
    }
```
定长：
```
    class StateArray{
        @Order(order = 1)
        short head;
        @Order(order = 2)
        byte cmd;
        @Length(length = 3)
        @Order(order = 11)
        State[] states;
        @Order(order = 100)
        byte sum;
    }

    class State {
        @Order(order = 1)
        byte key;
        @Order(order = 2)
        short value;
    }
```

如果在转换的过程中，需要用到一些额外加工途径才能生成最终的数据，或者在转换的过程中需要判断一些值的条件以决定后续；Byter也提供了相应的接口： ToBytesConverter 与 FromBytesConverter。
你可以通过重写 interceptToBytes 或者 interceptFromBytes 并通过返回true的形式自定义某些字段的转换结果。如果返回false则表示不对此字段进行拦截，一般情况下你只需要拦截需要特殊处理的字段即可。
如果在解析的过程中发现某些数据可能存在问题，你可以通过 throw new Execption 的方式及时抛出异常，以中止本次数据转换。
同时Byter还提供了 finishToBytes 和 finishFromBytes 方法，可以在最终转换数据前，做最终处理（加密解密等检验计算等）。

有一点需要特别指明：以上所有方法中都含有ByteBuffer参数，对此类不太熟悉的朋友一定要看ByteBuffer的使用事项。通过flip读取数据之后一定要再flip回来，并且read和get时也要保证没有多次调用。相信用过ByteBuffer的同学都明白我的意思，谨记谨记！

最后，都看到这儿，给个start再走吧。别别别走，我是开玩笑的，我是一个代码完美主义者，喜欢用优雅地方式解决一些常见的问题，如果你跟我志同道合，可以私聊或者邮件@我，欢迎一起搞基哦！
