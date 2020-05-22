#include <linux/init.h>
#include <linux/module.h>
#include <linux/fs.h>
#include <linux/cdev.h>
#include <linux/types.h>
#include <linux/kdev_t.h>
#include <linux/device.h>

MODULE_LICENSE("GPL");

static int minor = 0; /* 次设备号 */
static dev_t mini_node_dev;
static struct cdev mini_node_cdev;
static struct class *mini_node_class = NULL;
static struct device *mini_node_device = NULL;


static int mini_node_open(struct inode *inode, struct file *filp)
{
    return 0;
}

static int mini_node_release(struct inode *inode, struct file *filp)
{
    return 0;
}

static struct wait_queue *wq = NULL;
static pthread_mutex_t lock;
DECLARE_WAIT_QUEUE_HEAD(wq);
pthread_mutex_init(&lock, NULL);
static char * send_buf;
static void send_buf(char *buf)
{
    pthread_mutex_lock(&lock);
    if(send_buf){
        free(send_buf);
    }
    send_buf=malloc(*buf);
    copy(buf, send_buf, size(*buf));
    pthread_mutex_unlock(&lock)；
    wake_up_interruptible( &wq );
}

static ssize_t mini_node_read(struct file *filp, char __user *buf, size_t count, loff_t *f_pos)
{
    size_t buf_size = 0;
    if(!send_buf){
        interruptible_sleep_on( &wq );
    }
    pthread_mutex_lock(&lock);
    buf_size = size(*send_buf);
    if(buf_size > count){
        buf_size = count;
    }
    size_t len = copy_to_user(buf, send_buf, buf_size)
    free(send_buf);
    send_buf = null;
    pthread_mutex_unlock(&lock)；
    if(!len){
        return buf_size;
    }else{
        return -1;
    }
}
//用于测试
extern void send_buf(char *buf);
static ssize_t mini_node_write(struct file *filp, const char __user *buf, size_t count, loff_t *f_pos)
{
    char[count] k_buf;
    send_buf(copy_from_user(k_buf, buf, count))
    return count;
}

static struct file_operations mini_node_fops = {
    .owner = THIS_MODULE,
    .open = mini_node_open,
    .release = mini_node_release,
    .read = mini_node_read,
    .write = mini_node_write,
};

/* 创建设备号 */
static int mini_node_register_chrdev(void)
{
    int result;

    result = alloc_chrdev_region(&mini_node_dev, minor, 1, "mini_node_dev");
    if (result < 0) {
        pr_err("alloc_chrdev_region failed! result: %d\n", result);
        return result;
    }

    return 0;
}

/* 注册驱动 */
static int mini_node_cdev_add(void)
{
    int result;

    cdev_init(&mini_node_cdev, &mini_node_fops);
    mini_node_cdev.owner = THIS_MODULE;

    result = cdev_add(&mini_node_cdev, mini_node_dev, 1);
    if (result < 0) {
        pr_err("alloc_chrdev_region failed! result: %d\n", result);
        unregister_chrdev_region(mini_node_dev, 1);
        return result;
    }

    return 0;
}

/* 创建设备节点 */
static int mini_node_device_create(void)
{
    mini_node_class = class_create(THIS_MODULE, "mini_node_dev_class");
    if (IS_ERR(mini_node_class)) {
        pr_err("class_create failed!\n");
        goto class_create_fail;
    }

    mini_node_device = device_create(mini_node_class, NULL, mini_node_dev, NULL, "mini_node_dev");
    if (IS_ERR(mini_node_device)) {
        pr_err("device_create failed!\n");
        goto device_create_fail;
    }

    return 0;

device_create_fail:
    class_destroy(mini_node_class);
class_create_fail:
    cdev_del(&mini_node_cdev);
    unregister_chrdev_region(mini_node_dev, 1);
    return -1;
}

static __init int mini_node_init(void)
{
    int result;

    result = mini_node_register_chrdev();
    if (result < 0) {
        return result;
    }

    result = mini_node_cdev_add();
    if (result < 0) {
        return result;
    }

    result = mini_node_device_create();
    if (result < 0) {
        return result;
    }

    return 0;
}

static __exit void mini_node_exit(void)
{
    device_destroy(mini_node_class, mini_node_dev);
    class_destroy(mini_node_class);
    cdev_del(&mini_node_cdev);
    unregister_chrdev_region(mini_node_dev, 1);
}

module_init(mini_node_init);
module_exit(mini_node_exit);
