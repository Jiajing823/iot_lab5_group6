from machine import Pin, I2C, RTC
import ssd1306
import time
import network
import urequests
import ujson
import socket
i2c = I2C(scl=Pin(5), sda=Pin(4), freq=100000)
oled = ssd1306.SSD1306_I2C(128, 32, i2c)
rtc = RTC()
t_raw=rtc.datetime((2019,1,1,1,1,0,0,0))
t_raw=rtc.datetime()
t = list(t_raw)

def do_connect():
    sta_if = network.WLAN(network.STA_IF)
    if not sta_if.isconnected():
        print('connecting to network...')
        sta_if.active(True)
        sta_if.connect('Columbia University', ' ')
        while not sta_if.isconnected():
            pass
    print('network config:', sta_if.ifconfig())
    return sta_if.ifconfig()[0]

def rj2(s):
    if len(s)<2:
        s = '0'+s
    return s


def main():
    ipaddr = do_connect()
    addr= socket.getaddrinfo(ipaddr, 80)[0][-1]
    s=socket.socket()
    s.bind(addr)
    s.listen(1)
    print('listening on', addr)
    s.settimeout(0.5)
    flag= False
    while True:
        t_raw=rtc.datetime()
        try:
            res=s.accept()
        except OSError:
            print("Nothing happens.")
        else:
            c1=res[0]
            addr=res[1]
            print('client connected from', addr)
            print('client socket', c1)
            print('request')
            req=c1.recv(4096)
            html=req
            del(req)
            html=html.split(b'\r\n\r\n') #converts it into list
            raw_data=html[-1] #gets unsplited data
            data_decoded=raw_data.decode("utf-8") #byte to string conversion
            data=data_decoded.split("=",1) #split it using "=" sign
            sign=data[0]
            raw_context=data[1]
            context=raw_context.replace("+"," ")
            if sign=="TurnON":
                oled.fill(0)
                st_line1= "You are using" 
                st_line2= "The best watch" 
                st_line3= "In the world"
                oled.text(st_line1, 0, 0)
                oled.text(st_line2, 0, 10)
                oled.text(st_line3, 0, 20)
                oled.show()
                resp= "HTTP/1.1 200 OK\r\nContent-Type: application/text\r\nContent-Length: 10\r\n\r\n{'on'}"
                c1.send(resp)
                time.sleep(3)
            elif sign=="TurnOFF":
                oled.fill(0)
                oled.show()
                flag= False
                resp= "HTTP/1.1 200 OK\r\nContent-Type: application/text\r\nContent-Length: 10\r\n\r\n{'off'}"
                c1.send(resp)
                time.sleep(3)
            elif sign=="DisplayTime":
                flag=True
                resp= "HTTP/1.1 200 OK\r\nContent-Type: application/text\r\nContent-Length: 10\r\n\r\n{'time'}"
                c1.send(resp)
            elif sign=="message":    #max to max 21 characters
                if len(context)>21:
                    st="BIG MESSAGE"
                    oled.fill(0)
                    oled.text(st, 0, 10)
                    oled.show()
                    resp= "HTTP/1.1 200 OK\r\nContent-Type: application/text\r\nContent-Length: 10\r\n\r\n{'big mess'}"
                    c1.send(resp)
                    time.sleep(3)
                else:
                    oled.fill(0)
                    oled.text(context, 0, 10)
                    oled.show()
                    resp= "HTTP/1.1 200 OK\r\nContent-Type: application/text\r\nContent-Length: 10\r\n\r\n{'mess rec'}"
                    c1.send(resp)
                    time.sleep(3)
            else:
                resp= "HTTP/1.1 200 OK\r\nContent-Type: application/text\r\nContent-Length: 10\r\n\r\n{'nothing'}"
                c1.send(resp)
            c1.close()
        if flag==True:
                oled.fill(0)
                t_raw=rtc.datetime()
                t = list(t_raw) 
                st_line1 = str(t[0])+"/"+rj2(str(t[1]))+"/"+rj2(str(t[2]))
                st_line2 = rj2(str(t[4]))+":"+rj2(str(t[5]))+":"+rj2(str(t[6]))
                oled.text(st_line1, 0, 0)
                oled.text(st_line2, 0, 10)
                oled.show()
