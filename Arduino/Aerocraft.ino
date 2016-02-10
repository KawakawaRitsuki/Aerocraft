void setup() {
     pinMode(2,INPUT) ;    //スイッチに接続ピンをデジタル入力に設定
     pinMode(13,OUTPUT) ;  //LEDに接続ピンをデジタル出力に設定
     Serial.begin(9600);
}

boolean flag = false;
int count = 0;
void loop() {
    if (digitalRead(2) == HIGH) {     //スイッチの状態を調べる
        if(!flag){
            digitalWrite(13,HIGH);      //スイッチが押されているならLEDを点灯
            count = count + 1;
            Serial.println(count);
            flag = true;
        }
    } else {
        if(flag){
            flag = false;
            digitalWrite(13,LOW) ;       //スイッチが押されていないならLEDを消灯
        }
    }
}
