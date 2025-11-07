# Multi Window Patch - プロジェクト概要

## 🎯 プロジェクトの目的

Androidでマルチウィンドウを強制的に有効化し、アプリ起動中に**常時サイレント通知**を表示して、ワンタップでマルチウィンドウモードに切り替えられるようにするXposed/LSPosedモジュール。

## ✨ 主な機能

### 1. マルチウィンドウの強制有効化
- すべてのアプリでマルチウィンドウ機能を利用可能に
- システムレベルでの実装により、アプリ側の設定不要
- 分割画面、フリーフォームモード対応

### 2. 常時通知機能（新機能！）
- **サイレント通知**: 音や振動なし
- **常時表示**: アプリ起動中は常に表示
- **ワンタップ操作**: 通知のボタンからマルチウィンドウモードを開く
- **自動管理**: アプリ終了時に自動で通知も消える

## 📁 プロジェクト構造

```
multi/
├── README.md                    # 基本的な使い方
├── USAGE.md                     # 詳細な使用例とFAQ
├── DEVELOPER.md                 # 開発者向け技術ドキュメント
├── build.sh                     # ビルドスクリプト
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml          # マニフェスト（権限、サービス定義）
│   │   ├── assets/xposed_init           # Xposedエントリーポイント
│   │   └── java/.../multiwindowpatch/
│   │       ├── MainHook.java                      # メインフックロジック
│   │       ├── MultiWindowNotificationService.java # 通知サービス
│   │       └── MultiWindowActionReceiver.java     # 通知アクション処理
│   └── build.gradle             # 依存関係設定
└── build.gradle                 # プロジェクト設定
```

## 🔧 技術スタック

- **言語**: Java 8
- **最小SDK**: Android 7.0 (API 24)
- **対象SDK**: Android 14 (API 34)
- **フレームワーク**: Xposed/LSPosed
- **主要ライブラリ**: 
  - Xposed API 82
  - AndroidX Core 1.12.0

## 🚀 クイックスタート

### 前提条件
- Root化されたAndroidデバイス
- LSPosed または Xposed Framework
- JDK 8以降
- Android SDK

### ビルド＆インストール

```bash
# リポジトリをクローン
git clone <repository-url>
cd multi

# ビルド
./build.sh

# または
./gradlew assembleRelease

# APKをインストール
adb install app/build/outputs/apk/release/app-release.apk

# LSPosedで有効化
# 1. LSPosedマネージャーを開く
# 2. モジュールタブで「Multi Window Patch」を有効化
# 3. スコープで「System Framework (android)」を選択
# 4. デバイスを再起動
```

## 📱 使い方

### 基本的な流れ

1. **アプリを起動** → 自動的に通知が表示される
2. **通知を展開** → 「マルチウィンドウで開く」ボタンが表示される
3. **ボタンをタップ** → アプリがマルチウィンドウモードで起動

### 通知の特徴

- ✅ サイレント（音・振動なし）
- ✅ 常時表示（スワイプで消せない）
- ✅ 低優先度（邪魔にならない）
- ✅ システムアプリでは非表示

## 🔍 仕組み

### 1. システムフレームワークのフック
```java
// ActivityInfo.isResizeable() → 常にtrue
// ActivityRecord → resizeMode を変更
// PackageParser → パッケージ解析時に設定
```

### 2. アプリライフサイクルの監視
```java
// Activity.onResume() → 通知表示
// Activity.onPause() → バックグラウンド処理
// Activity.onDestroy() → 通知削除
```

### 3. 通知システム
```java
// MultiWindowNotificationService → フォアグラウンドサービス
// MultiWindowActionReceiver → ボタンタップ処理
// NotificationChannel → サイレント通知チャンネル
```

## 📋 必要な権限

- `POST_NOTIFICATIONS` - 通知表示（Android 13以降）
- `FOREGROUND_SERVICE` - フォアグラウンドサービス
- `FOREGROUND_SERVICE_SPECIAL_USE` - 特殊用途のFGS
- `SYSTEM_ALERT_WINDOW` - システムオーバーレイ

## 🎨 カスタマイズ

### 除外アプリの追加

`MainHook.java`:
```java
EXCLUDED_PACKAGES.add("com.example.app");
```

### 通知のカスタマイズ

`MultiWindowNotificationService.java`:
```java
.setContentTitle("カスタムタイトル")
.setContentText("カスタムテキスト")
```

## 🐛 トラブルシューティング

### 通知が表示されない
- [ ] モジュールが有効化されているか確認
- [ ] デバイスを再起動
- [ ] 通知権限を確認（Android 13以降）
- [ ] LSPosedログを確認

### マルチウィンドウが動作しない
- [ ] スコープに「android」が含まれているか確認
- [ ] デバイスがマルチウィンドウをサポートしているか確認
- [ ] アプリを再起動

詳細は `USAGE.md` と `DEVELOPER.md` を参照。

## 📚 ドキュメント

- **README.md** - 基本情報、インストール方法、使い方
- **USAGE.md** - 詳細な使用例、FAQ、トラブルシューティング
- **DEVELOPER.md** - 技術詳細、API説明、カスタマイズ方法

## ⚠️ 注意事項

- 一部のアプリでは正しく動作しない場合があります
- セキュリティアプリや銀行アプリでは問題が発生する可能性があります
- 使用は自己責任でお願いします

## 📄 ライセンス

MIT License

## 🤝 貢献

バグ報告、機能要望、プルリクエストは大歓迎です！

---

**開発者**: tqmane  
**バージョン**: 1.0  
**最終更新**: 2025年11月7日
