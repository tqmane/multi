# 開発者向けドキュメント

## プロジェクト構造

```
multi/
├── app/
│   ├── build.gradle                    # アプリモジュールのビルド設定
│   ├── proguard-rules.pro              # ProGuard設定
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml     # マニフェストファイル
│           ├── assets/
│           │   └── xposed_init         # Xposedエントリーポイント
│           ├── java/
│           │   └── com/tqmane/multiwindowpatch/
│           │       ├── MainHook.java                      # メインのフックロジック
│           │       ├── MultiWindowNotificationService.java # 通知サービス
│           │       └── MultiWindowActionReceiver.java     # 通知アクションレシーバー
│           └── res/                    # リソースファイル
├── build.gradle                        # プロジェクトレベルのビルド設定
├── settings.gradle                     # Gradleプロジェクト設定
├── gradle.properties                   # Gradle設定
├── build.sh                           # ビルドスクリプト
└── README.md                          # 使用方法
```

## 技術詳細

### アーキテクチャ

このモジュールは3つの主要コンポーネントで構成されています：

#### 1. MainHook (Xposedフックロジック)

**システムフレームワークのフック**:
- `android`パッケージをフックしてマルチウィンドウ機能を有効化
- ActivityInfo、ActivityRecord、PackageParserをフック

**アプリライフサイクルのフック**:
- すべてのアプリの`Activity.onResume()`をフック
- アプリが前面に来た時に通知サービスを起動
- アプリ終了時に通知サービスを停止

#### 2. MultiWindowNotificationService (フォアグラウンドサービス)

**役割**:
- アプリ起動中に常時通知を表示
- サイレント通知チャンネルを使用
- フォアグラウンドサービスとして動作

**通知の特徴**:
- Priority: LOW（サイレント）
- Ongoing: true（スワイプで消せない）
- アクションボタン付き

#### 3. MultiWindowActionReceiver (BroadcastReceiver)

**役割**:
- 通知のボタンタップを処理
- 指定されたアプリをマルチウィンドウモードで起動
- `FLAG_ACTIVITY_LAUNCH_ADJACENT`フラグを使用

### フックポイント

このモジュールは以下のメソッドをフックします：

#### システムフレームワーク関連

##### 1. ActivityInfo.isResizeable()

```java
android.content.pm.ActivityInfo.isResizeable()
```

**目的**: アクティビティがリサイズ可能かどうかを判定するメソッドをフックし、常に`true`を返すようにします。

**実装**:
- 戻り値を強制的に`true`に変更
- これにより、システムはすべてのアクティビティをリサイズ可能と認識

#### 2. ActivityRecord コンストラクタ

```java
com.android.server.wm.ActivityRecord(ActivityTaskManagerService, int, ActivityInfo)
```

**目的**: アクティビティレコードが作成される際に、マルチウィンドウ関連の設定を変更します。

**実装**:
- `ActivityInfo.resizeMode`を`RESIZE_MODE_RESIZEABLE`(2)に設定
- アクティビティが作成される時点でマルチウィンドウ対応に変更

##### 3. PackageParser.generateActivityInfo()

```java
android.content.pm.PackageParser.generateActivityInfo(Activity, int, PackageUserState, int)
```

**目的**: APKのパース時にActivityInfoを生成する際、マルチウィンドウフラグを設定します。

**実装**:
- 生成されたActivityInfoの`resizeMode`を変更
- パッケージインストール時から対応させる

#### アプリライフサイクル関連

##### 4. Activity.onResume()

```java
android.app.Activity.onResume()
```

**目的**: アプリが前面に来た時に通知を表示

**実装**:
- アプリがフォアグラウンドになった時にMultiWindowNotificationServiceを起動
- 除外リストに含まれるアプリ（システムアプリなど）はスキップ

##### 5. Activity.onPause()

```java
android.app.Activity.onPause()
```

**目的**: アプリがバックグラウンドに移行した時の処理

**実装**:
- アプリが完全に終了する場合のみ通知を非表示
- 単にバックグラウンドに行った場合は通知を維持

##### 6. Activity.onDestroy()

```java
android.app.Activity.onDestroy()
```

**目的**: アプリ終了時に通知を削除

**実装**:
- MultiWindowNotificationServiceを停止
- フォアグラウンドサービスを解除

### ResizeMode の種類

Android 7.0以降で定義されているresizeModeの値：

```java
RESIZE_MODE_UNRESIZEABLE = 0            // リサイズ不可
RESIZE_MODE_RESIZEABLE_VIA_SDK_VERSION = 1  // SDKバージョンに応じて
RESIZE_MODE_RESIZEABLE = 2              // リサイズ可能（推奨）
RESIZE_MODE_FORCE_RESIZEABLE = 3        // 強制的にリサイズ可能
RESIZE_MODE_FORCE_RESIZABLE_PORTRAIT_ONLY = 4   // 縦向きのみ
RESIZE_MODE_FORCE_RESIZABLE_LANDSCAPE_ONLY = 5  // 横向きのみ
RESIZE_MODE_FORCE_RESIZABLE_PRESERVE_ORIENTATION = 6  // 向きを保持
RESIZE_MODE_RESIZEABLE_AND_PIPABLE_DEPRECATED = 7    // 非推奨
```

このモジュールでは`RESIZE_MODE_RESIZEABLE` (2)を使用しています。

## ビルド要件

- JDK 8以降
- Android SDK (API Level 24以降)
- Gradle 8.0以降

## デバッグ方法

### LSPosedログの確認

1. LSPosedマネージャーを開く
2. 「ログ」セクションに移動
3. 「MultiWindowPatch」でフィルタ

### Logcatでの確認

```bash
adb logcat | grep "MultiWindowPatch"
```

### フックが適用されているか確認

以下のログメッセージを確認：
- `MultiWindowPatch: Hooking into Android framework`
- `MultiWindowPatch: Hooked ActivityInfo.isResizeable()`
- `MultiWindowPatch: Successfully hooked multi-window checks`

## よくある問題

### 1. モジュールが読み込まれない

**原因**: スコープが正しく設定されていない

**解決策**: 
- LSPosedマネージャーでモジュールのスコープを確認
- 「System Framework (android)」が選択されているか確認

### 2. 一部のアプリで動作しない

**原因**: アプリ側で追加のチェックを実装している可能性

**解決策**:
- 該当アプリのログを確認
- 必要に応じて追加のフックポイントを実装

### 3. システムが不安定になる

**原因**: 一部のシステムアプリがマルチウィンドウに対応していない

**解決策**:
- モジュールを無効化
- セーフモードで起動してモジュールを削除

## カスタマイズ

### 特定のアプリを除外したい場合

`MainHook.java`の`EXCLUDED_PACKAGES`セットにパッケージ名を追加：

```java
private static final Set<String> EXCLUDED_PACKAGES = new HashSet<>();

static {
    EXCLUDED_PACKAGES.add("android");
    EXCLUDED_PACKAGES.add("com.android.systemui");
    EXCLUDED_PACKAGES.add("com.android.settings");
    EXCLUDED_PACKAGES.add("com.tqmane.multiwindowpatch");
    // 追加したいパッケージ
    EXCLUDED_PACKAGES.add("com.example.app");
}
```

### 通知のカスタマイズ

`MultiWindowNotificationService.java`で通知の内容を変更できます：

```java
// 通知のタイトルとテキストを変更
.setContentTitle("カスタムタイトル")
.setContentText("カスタムテキスト")

// 通知の優先度を変更
.setPriority(NotificationCompat.PRIORITY_DEFAULT)

// アイコンを変更
.setSmallIcon(R.drawable.custom_icon)
```

### マルチウィンドウ起動ロジックのカスタマイズ

`MultiWindowActionReceiver.java`で起動方法を変更できます：

```java
// フリーフォームモードで起動（Android 11以降）
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    ActivityOptions options = ActivityOptions.makeBasic();
    options.setLaunchWindowingMode(WINDOWING_MODE_FREEFORM);
    context.startActivity(launchIntent, options.toBundle());
}
```

### ログレベルの変更

詳細なログを出力したい場合は、各フックメソッドに追加のログ出力を追加します。

## 貢献ガイドライン

1. フォークしてブランチを作成
2. 変更を実装
3. テストを実施
4. プルリクエストを作成

## ライセンス

MIT License - 詳細は LICENSE ファイルを参照
