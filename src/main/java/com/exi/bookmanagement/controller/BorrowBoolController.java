package com.exi.bookmanagement.controller;

import com.alibaba.fastjson.JSON;
import com.exi.bookmanagement.entity.Book;
import com.exi.bookmanagement.entity.BorrowBook;
import com.exi.bookmanagement.entity.BorrowBookHis;
import com.exi.bookmanagement.mapper.BookMapper;
import com.exi.bookmanagement.mapper.BorrowBookHisMapper;
import com.exi.bookmanagement.mapper.BorrowBookMapper;
import com.exi.bookmanagement.response.BorrowBookResponse;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Copyright: Copyright (c) 2021 Asiainfo
 *
 * @ClassName: com.exi.bookmanagement.controller.BorrowBoolController
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: Fengsx
 * @date: 2021/1/28 18:23
 * <p>
 * Modification History:
 * Date       Author    Version    Description
 * ----------------------------------------------------------
 * 2021/1/28    Fengsx     v1.0.0      修改原因
 */
@Api(tags = "读者借阅纸质书管理")
@RestController
@CrossOrigin
@Transactional
@Slf4j
@RequestMapping(value = "/borrowBook")
public class BorrowBoolController {
    @Autowired
    private BorrowBookMapper borrowBookMapper;

    @Autowired
    private BorrowBookHisMapper borrowBookHisMapper;

    @Autowired
    private BookMapper bookMapper;


    @ApiOperation("读者取消预约")
    @GetMapping(value = "/cancelBorrowBook/{borBookId}")
    public BorrowBookResponse cancelBorrowBook(@PathVariable("borBookId") Long borBookId) throws ParseException {
        BorrowBookResponse borrowBookResponse = new BorrowBookResponse();
        BorrowBookHis oneBorrowBookHisBean = borrowBookHisMapper.getOneBorrowBookHisBean(borBookId);
        oneBorrowBookHisBean.setState(5);
        try{
            borrowBookHisMapper.updateBorrowBookHisBean(oneBorrowBookHisBean);
            log.info("oneBorrowBookHisBean:{}",JSON.toJSONString(oneBorrowBookHisBean));
            borrowBookResponse.setCode(20000);
            borrowBookResponse.setMessage("取消预约成功~");
            return borrowBookResponse;
        }catch (Exception e){
            e.printStackTrace();
            borrowBookResponse.setCode(88888);
            borrowBookResponse.setMessage("取消预约失败");
            return borrowBookResponse;
        }

    }

    @ApiOperation("读者借书")
    @GetMapping(value = "/borrowBook/{borBookId}")
    public BorrowBookResponse borrowBook(@PathVariable("borBookId") Long borBookId) throws ParseException {
        BorrowBookResponse borrowBookResponse = new BorrowBookResponse();
        BorrowBookHis oneBorrowBookHisBean = borrowBookHisMapper.getOneBorrowBookHisBean(borBookId);
        oneBorrowBookHisBean.setState(4);
        //借书时间
        Date time = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String giveBookTime = sdf.format(time);
        oneBorrowBookHisBean.setGiveBookTime(giveBookTime);
        try{
            borrowBookHisMapper.updateBorrowBookHisBean(oneBorrowBookHisBean);
            log.info("oneBorrowBookHisBean:{}",JSON.toJSONString(oneBorrowBookHisBean));
            borrowBookResponse.setCode(20000);
            borrowBookResponse.setMessage("借书成功~");
            return borrowBookResponse;
        }catch (Exception e){
            e.printStackTrace();
            borrowBookResponse.setCode(88888);
            borrowBookResponse.setMessage("借书失败");
            return borrowBookResponse;
        }

    }

    @ApiOperation("读者还书")
    @GetMapping(value = "/getBackBook/{borBookId}")
    public BorrowBookResponse getBackBook(@PathVariable("borBookId") Long borBookId) throws ParseException {
        BorrowBookResponse borrowBookResponse = new BorrowBookResponse();
        BorrowBookHis oneBorrowBookHisBean = borrowBookHisMapper.getOneBorrowBookHisBean(borBookId);
        Date time = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String expectGetBackTime = oneBorrowBookHisBean.getExpectGetBackTime();
        Date expectGetBackTime1 = sdf.parse(expectGetBackTime);
        String giveBackBookTime = sdf.format(time);
        Long bookByBorBookId = borrowBookMapper.getBookByBorBookId(borBookId);
        Book oneBookBeanById = bookMapper.getOneBookBeanById(bookByBorBookId);
        if (expectGetBackTime1.after(time)){
            oneBorrowBookHisBean.setGetBackBookTime(giveBackBookTime);
            oneBorrowBookHisBean.setState(1);
            oneBookBeanById.setBookRepertory(oneBookBeanById.getBookRepertory() + oneBorrowBookHisBean.getBorBookNum());
            //图书库存加回来
            try {
                bookMapper.updateBookBean(oneBookBeanById);
                borrowBookHisMapper.updateBorrowBookHisBean(oneBorrowBookHisBean);
                borrowBookResponse.setCode(20000);
                borrowBookResponse.setMessage("还书成功~");
                return borrowBookResponse;
            }catch (Exception e){
                e.printStackTrace();
                borrowBookResponse.setCode(88888);
                borrowBookResponse.setMessage("还书失败");
                return borrowBookResponse;
            }
        }
        try {
            oneBorrowBookHisBean.setGetBackBookTime(giveBackBookTime);
            oneBorrowBookHisBean.setState(2);
            oneBorrowBookHisBean.setBooleanLate(1);
            oneBookBeanById.setBookRepertory(oneBookBeanById.getBookRepertory() + oneBorrowBookHisBean.getBorBookNum());
            bookMapper.updateBookBean(oneBookBeanById);
            borrowBookHisMapper.updateBorrowBookHisBean(oneBorrowBookHisBean);
            borrowBookResponse.setCode(20000);
            borrowBookResponse.setMessage("还书成功,你已预期");
            return borrowBookResponse;
        }catch (Exception e){
            e.printStackTrace();
            borrowBookResponse.setCode(88888);
            borrowBookResponse.setMessage("还书失败");
            return borrowBookResponse;
        }
    }

    @ApiOperation("分页查询指定读者纸质书借阅记录")
    @GetMapping(value = "/getReadBooksPage/{pageNum}/{pageSize}/{readerId}")
    public BorrowBookResponse getReadBooksPage(@PathVariable("pageNum") Integer pageNum, @PathVariable("pageSize") Integer pageSize, @PathVariable("readerId") Long readerId){
        BorrowBookResponse borrowBookResponse = new BorrowBookResponse();
        Page<BorrowBook> pageInfo = PageHelper.startPage(pageNum, pageSize);
        if (pageInfo.getPageNum() == 0 || pageInfo.getPageSize() == 0) {
            log.info("pageNum || pageSize 有值为空");
        }
        //并查询
        PageHelper.startPage(pageInfo.getPageNum(), pageInfo.getPageSize());
        List<BorrowBook> borrowBookList = borrowBookMapper.getBorrowBookListByBookIdAndBorBookId(readerId);
        if (CollectionUtils.isEmpty(borrowBookList)){
            borrowBookResponse.setCode(88888);
            borrowBookResponse.setMessage("你没有纸质书借阅纸质书的记录哦~");
            return borrowBookResponse;
        }

        // 如果在获取到数据之后就对数据进行转dto操作的话，会获取不到total数据，所以又定义了一个PageInfo类然后将数据进行属性复制，来获取数据
/*        try {
            //查询之前先更新一波借阅状态
            updateBorBookStateService.updateBookState();

        }catch (Exception e){
            borrowBookResponse.setCode(88888);
            borrowBookResponse.setMessage("更新纸质图书库存失败啦");
            e.printStackTrace();
            return borrowBookResponse;
        }*/
        PageInfo<BorrowBook> pageInfo1 = new PageInfo<>();
        BeanUtils.copyProperties(new PageInfo<>(borrowBookList), pageInfo1);
        log.info("封装后的 pageInfo:{}",JSON.toJSONString(pageInfo1));
        // 定义一个 response 把状态码和 message 加到 response 里面，不然前台会拒绝请求
        borrowBookResponse.setCode(20000);
        borrowBookResponse.setMessage("返回 date 为 返回的是读者纸质书借阅纸质书的记录");
        borrowBookResponse.setPageInfo(pageInfo1);
        return borrowBookResponse;
    }

    //按书名分组，按读者分组
    @ApiOperation("分页查询所有读者电子图书纸质书借阅记录")
    @GetMapping(value = "/getAllReadBooksPage/{pageNum}/{pageSize}")
    public BorrowBookResponse getAllReadBooksPage(@PathVariable("pageNum") Integer pageNum, @PathVariable("pageSize") Integer pageSize){
        BorrowBookResponse borrowBookResponse = new BorrowBookResponse();
        Page<BorrowBook> pageInfo = PageHelper.startPage(pageNum, pageSize);
        if (pageInfo.getPageNum() == 0 || pageInfo.getPageSize() == 0) {
            log.info("pageNum || pageSize 有值为空");
        }
        //并查询
        PageHelper.startPage(pageInfo.getPageNum(), pageInfo.getPageSize());
        List<BorrowBook> borrowBookList = borrowBookMapper.getAllBorrowBookByBookIdAndBorBookIdAndReaderId();
        if (CollectionUtils.isEmpty(borrowBookList)){
            borrowBookResponse.setCode(88888);
            borrowBookResponse.setMessage("没有纸质书借阅纸质书的记录哦~");
            return borrowBookResponse;
        }
        // 如果在获取到数据之后就对数据进行转dto操作的话，会获取不到total数据，所以又定义了一个PageInfo类然后将数据进行属性复制，来获取数据
        PageInfo<BorrowBook> pageInfo1 = new PageInfo<>();
        BeanUtils.copyProperties(new PageInfo<>(borrowBookList), pageInfo1);
        log.info("封装后的 pageInfo:{}",pageInfo1);
        // 定义一个 response 把状态码和 message 加到 response 里面，不然前台会拒绝请求
        borrowBookResponse.setCode(20000);
        borrowBookResponse.setMessage("返回 date 为 返回的是全部读者纸质书借阅纸质书的记录");
        borrowBookResponse.setPageInfo(pageInfo1);
        return borrowBookResponse;
    }

    //读者点击纸质书借阅纸质书按钮
    @ApiOperation("生成纸质书借阅记录")
    @Transactional
    @GetMapping(value = "/updateBorrowBookReadHis/{readerId}/{bookId}/{expectGetBackTime}/{borBookNum}")
    public BorrowBookResponse updateBorrowBookReadHis(@PathVariable("readerId")Long readerId, @PathVariable("bookId")Long bookId,
                                                      @PathVariable("expectGetBackTime")Date expectGetBackTime, @PathVariable("borBookNum")Long borBookNum){
        BorrowBookResponse borrowBookResponse = new BorrowBookResponse();
        BorrowBook borrowBook = new BorrowBook();
        BorrowBookHis borrowBookHis = new BorrowBookHis();
            try {
                borrowBook.setReaderId(readerId);
                borrowBook.setBookId(bookId);
                log.info("readBook:{}", JSON.toJSONString(borrowBook));

                //预约借书时间即当前时间
                Date time = new Date(System.currentTimeMillis());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String giveBookTime = sdf.format(time);
                borrowBookHis.setSubscribeTime(giveBookTime);
                Book oneBookBeanById = bookMapper.getOneBookBeanById(bookId);
                if (oneBookBeanById.getBookRepertory() < borBookNum || borBookNum == 0){
                    borrowBookResponse.setCode(88888);
                    borrowBookResponse.setMessage("借阅数量不能大于库存且库存不能为哦");
                    return borrowBookResponse;
                }
                borrowBookHis.setBorBookNum(borBookNum);
                String expectGetBackTime1 = sdf.format(expectGetBackTime);
                if (expectGetBackTime.before(time)){
                    borrowBookResponse.setCode(88888);
                    borrowBookResponse.setMessage("预期还书时间必须大于一天哦");
                    return borrowBookResponse;
                }

                borrowBookHis.setExpectGetBackTime(expectGetBackTime1);

                borrowBookHis.setBooleanLate(0);
                //预约成功
                borrowBookHis.setState(0);
                borrowBookMapper.insertBorrowBookBean(borrowBook);
                //获取插入自增主键的 id
                long borBookId = borrowBook.getBorBookId();
                borrowBookHis.setBorBookId(borBookId);
                borrowBookHisMapper.insertBorrowBookHisBean(borrowBookHis);

                //更新纸质图书库存
                oneBookBeanById.setBookRepertory(oneBookBeanById.getBookRepertory() - borBookNum);
                int i = bookMapper.updateBookBean(oneBookBeanById);
                if (i == 0){
                    borrowBookResponse.setCode(88888);
                    borrowBookResponse.setMessage("更新纸质图书库存失败啦");
                    return borrowBookResponse;
                }
                log.info("oneBookBeanById:{}", JSON.toJSONString(oneBookBeanById));
                log.info("borrowBookHis:{}", JSON.toJSONString(borrowBookHis));
                borrowBookResponse.setBook(oneBookBeanById);
                borrowBookResponse.setBorrowBook(borrowBook);
                borrowBookResponse.setBorrowBookHis(borrowBookHis);
                borrowBookResponse.setCode(20000);
                borrowBookResponse.setMessage("返回的是生成第一次的纸质书借阅记录");
                return borrowBookResponse;
            }catch (Exception e){
                System.out.println(e);
                borrowBookResponse.setCode(888888);
                borrowBookResponse.setMessage("生成第一次纸质书借阅记录出错啦");
                return borrowBookResponse;
            }
    }

}
