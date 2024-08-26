# 樹梅派 nl80211 wifi 驅動
> README Author: yomisana  
> 使用 Proxmox ve 8.2.4 LXC 容器安裝 Ubuntu 22.04 操作

# 虛擬機規格
- CPU: 越多越好 (分配 24 core，使用 E5 2697v2)
- RAM: 16GB 以上 (峰值 10.13G)
- DISK: 64GB 以上 (包含系統整體占用 42.06 GiB)

# 目錄
- [樹梅派 nl80211 wifi 驅動](#樹梅派-nl80211-wifi-驅動)
- [虛擬機規格](#虛擬機規格)
- [目錄](#目錄)
- [安裝步驟復刻](#安裝步驟復刻)
  - [虛擬機更新與安裝依賴](#虛擬機更新與安裝依賴)
  - [建置 yocto](#建置-yocto)
  - [導入 meta 依賴](#導入-meta-依賴)
  - [複製/修改設定檔](#複製修改設定檔)
    - [local.conf](#localconf)
    - [bblayers.conf](#bblayersconf)
    - [啟動環境](#啟動環境)
    - [raspberrypi3 nl80211 wifi 驅動添加](#raspberrypi3-nl80211-wifi-驅動添加)
    - [導入自訂義測試驅動](#導入自訂義測試驅動)
  - [開始構建](#開始構建)
    - [如果在 root 下執行](#如果在-root-下執行)
  - [結果](#結果)
  - [特殊指令](#特殊指令)


# 安裝步驟復刻
> 依據 xuan 給的步驟並且在優化
- 在指令上使用 ~ 代表使用者目錄，避免他人復刻時出現問題
## 虛擬機更新與安裝依賴
```
apt-get update; apt-get -y install gawk wget git diffstat unzip texinfo gcc-multilib build-essential chrpath socat cpio python3 python3-pip python3-pexpect xz-utils debianutils iputils-ping zstd tmux;git --version;chrpath --version
```
輸出:
```
...安裝過程...
> git version 2.34.1
> chrpath version 0.16
```

## 建置 yocto
```
mkdir -p ~/yocto/source;cd ~/yocto/source;pwd;git clone git://git.yoctoproject.org/poky -b dunfell;cd poky
```
輸出:
```
>root@xuan-Debug-PC:~/yocto/source# pwd
>/root/yocto/source
git clone 中...
```

## 導入 meta 依賴
```
git clone git://git.yoctoproject.org/meta-raspberrypi -b dunfell;git clone https://git.openembedded.org/meta-openembedded -b dunfell
```
輸出:
```
git clone 中...
```
## 複製/修改設定檔
> 修改建置設定檔
### local.conf
```
cp conf/local.conf conf/local_backup.conf;nano conf/local.conf
```
修改文件資訊:
```
除了 Machine ?? = "raspberrypi3"
其餘添加在 Machine ?? 下方
IMAGE_INSTALL:append = "mymodule wpa-supplicant-cli wpa-supplicant linux-firmware-rpidistro-bcm43430 iw"
DISTRO_FEATURES:append = "wifi systemd cfg80211 nl80211"
VIRTUAL-RUNTIME_init_manager = "systemd"
VIRTUAL-RUNTIME_initscripts = ""
KERNEL_SRC = "/{userdir}/yocto/source/poky/build/tmp/work-shared/raspberrypi3/kernel-source"
```
- 請注意 {userdir} 請替換為自己的使用者目錄
save and exit

### bblayers.conf
```
cp conf/bblayers.conf conf/bblayers_backup.confnano;conf/bblayers.conf
```
修改文件資訊:
```
僅修改 BBLAYERS 即可其餘不需要動
# POKY_BBLAYERS_CONF_VERSION is increased each time build/conf/bblayers.conf
# changes incompatibly
POKY_BBLAYERS_CONF_VERSION = "2"

BBPATH = "${TOPDIR}"
BBFILES ?= ""

BBLAYERS ?= " \
  /{userdir}/yocto/source/poky/meta \
  /{userdir}/yocto/source/poky/meta-poky \
  /{userdir}/yocto/source/poky/meta-yocto-bsp \
  /{userdir}/yocto/source/poky/meta-raspberrypi \
  /{userdir}/yocto/source/poky/meta-openembedded/meta-oe \
  /{userdir}/yocto/source/poky/meta-openembedded/meta-python \
  /{userdir}/yocto/source/poky/meta-openembedded/meta-networking \
  /{userdir}/yocto/source/poky/meta-mylayer \
  "
```
- 請注意 {userdir} 請替換為自己的使用者目錄
save and exit

### 啟動環境 
```
cd ~/yocto/source/poky/;source oe-init-build-env
```

### raspberrypi3 nl80211 wifi 驅動添加
```
bitbake linux-raspberrypi -c menuconfig
```
選擇 
- Networking support
  - Wireless
    - cfg80211 - wireless configuration API
    - nl80211 - new netlink-based wireless configuration API
    - mac80211 - IEEE 802.11 mesh, monitor, etc
save and exit

建置 raspberrypi3 系統並添加config上的開啟的 nl80211 驅動
```
bitbake linux-raspberrypi -c compile
```

### 導入自訂義測試驅動
```
cd ~/yocto/source/poky/;git clone https://github.com/Xuan901001/meta-mylayer.git
```

## 開始構建
> 準備開始建構
### 如果在 root 下執行
```
nano ~/yocto/source/poky/build/conf/local.conf
```
修改文件資訊:
```
新增此行
> INHERIT:remove = "sanity"
```
save and exit

開始建置:
```
#bitbake linux-raspberrypi -c menuconfig
bitbake core-image-full-cmdline
```

## 結果
> 查看映像檔 燒錄
```
cd ~/yocto/source/poky/build/tmp/deploy/images/raspberrypi3/
```
所需文件:
```
> core-image-full-cmdline-raspberrypi3-20240826063701.rootfs.wic
```

## 特殊指令
> 清理先前的構建結果  
bitbake -c clean core-image-full-cmdline

> 構建映像  
bitbake core-image-full-cmdline

> 模塊是否在內核中?  
> lsmod | grep cfg80211

> 查看 layer 依賴是否存在可寫入?
> bitbake-layers show-appends | grep wpa-supplicant