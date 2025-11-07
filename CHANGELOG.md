# 変更履歴

## [1.0.0] - 2025-11-07

### 追加機能

#### マルチウィンドウ強制有効化
- すべてのアプリでマルチウィンドウ機能を利用可能に
- ActivityInfo.isResizeable()のフック実装
- ActivityRecordコンストラクタのフック実装
- PackageParser.generateActivityInfo()のフック実装

#### 通知システム（新機能）
- アプリ起動中に常時サイレント通知を表示
- 通知からワンタップでマルチウィンドウモードを開く機能
- MultiWindowNotificationServiceの実装
- MultiWindowActionReceiverの実装
- フォアグラウンドサービスとして動作

#### アプリライフサイクル監視
- Activity.onResume()フック - アプリ前面表示時に通知表示
- Activity.onPause()フック - バックグラウンド移行時の処理
- Activity.onDestroy()フック - アプリ終了時に通知削除

#### 除外機能
- システムアプリを除外（android, systemui, settings）
- カスタマイズ可能な除外リスト

### 技術詳細

- **最小SDK**: Android 7.0 (API 24)
- **対象SDK**: Android 14 (API 34)
- **Xposed API**: 82
- **言語**: Java 8
- **ビルドツール**: Gradle 8.0

### 権限

- POST_NOTIFICATIONS - 通知表示
- FOREGROUND_SERVICE - フォアグラウンドサービス
- FOREGROUND_SERVICE_SPECIAL_USE - 特殊用途FGS
- SYSTEM_ALERT_WINDOW - システムオーバーレイ

### ドキュメント

- README.md - 基本的な使い方
- USAGE.md - 詳細な使用例とFAQ
- DEVELOPER.md - 開発者向け技術ドキュメント
- PROJECT_OVERVIEW.md - プロジェクト概要
- CHANGELOG.md - 変更履歴

### 既知の問題

- 一部のアプリ（ゲーム、銀行アプリなど）では正しく動作しない場合があります
- Android 13以降では通知権限の手動許可が必要な場合があります

### 今後の予定

- [ ] 通知のカスタマイズオプション追加
- [ ] 除外リストのUI設定画面追加
- [ ] フリーフォームモードの詳細設定
- [ ] パフォーマンス最適化
