#!/bin/bash

# Multi Window Patch ビルドスクリプト

echo "================================"
echo "Multi Window Patch - ビルド開始"
echo "================================"

# プロジェクトをクリーン
echo "プロジェクトをクリーンしています..."
./gradlew clean

# リリースビルドを作成
echo "リリースビルドを作成しています..."
./gradlew assembleRelease

# ビルド結果を確認
if [ $? -eq 0 ]; then
    echo ""
    echo "================================"
    echo "✅ ビルドが成功しました！"
    echo "================================"
    echo ""
    echo "APKの場所:"
    echo "app/build/outputs/apk/release/app-release.apk"
    echo ""
    echo "インストール方法:"
    echo "1. 上記のAPKをデバイスにインストール"
    echo "2. LSPosed/Xposedマネージャーでモジュールを有効化"
    echo "3. スコープで 'System Framework (android)' を選択"
    echo "4. デバイスを再起動"
    echo ""
else
    echo ""
    echo "================================"
    echo "❌ ビルドが失敗しました"
    echo "================================"
    echo ""
    echo "エラーログを確認してください"
    exit 1
fi
