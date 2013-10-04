[開発環境セットアップ](https://github.com/usuyama/hachiko-client/wiki/%E9%96%8B%E7%99%BA%E7%92%B0%E5%A2%83%E3%81%AE%E3%82%BB%E3%83%83%E3%83%88%E3%82%A2%E3%83%83%E3%83%97)

### パッケージ
分け方試行錯誤中．今の分け方と，大体の役割をメモる
* apis: サーバとの連携まわり
* data: なんらかの値を格納するためのクラスか，enum
* db: Android端末上のSQLite関係
* dev: 開発中にのみ用いられるべきクラスたち．ダミーデータを返すRequestQueueとか
* friends: 電話帳，あるいはFacebook上の友達に関するもの
* plans: 予定に関するもの．いちばん多くなりそう
* prefs: 設定画面とSharedPreferenceまわり
* push: GCM関係
* setup: 初回起動時の設定画面まわり
* ui: パッケージをまたがって使われる（であろう)Viewとか
* util: オレオレ便利クラス

### ライブラリ
将来的にはMavenとかで管理したいけど，チームメンバの動向も見つつ，という感じ

* android-support-v4: Android公式の下方互換用ライブラリ
* guava: Google謹製の便利ライブラリ，Javaのつらみを減らしてくれる．ImmutableListとか，Lists.apply(Function)とか
* volley: Google謹製の通信用ライブラリ

***

開発途中でアプリがローカルに保存してるデータを消したりしたくなる．[このアプリ](https://github.com/usuyama/hachiko-client/wiki/raw/HachikoOpenAppManager.zip)
をインストールしておくと，Android標準のアプリマネージャのHachikoに対応するページを1クリックで開けて便利．