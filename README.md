# Player Radar Mod

Minecraft 26.2 のLocator Barでクライアントが受信した他プレイヤーの位置を、JourneyMapのマーカーオーバーレイとして表示するFabricクライアントMODです。

## 必要環境

- Minecraft 26.2
- Fabric Loader 0.19.3 以降
- Fabric API 0.156.0+26.2 以降
- JourneyMap 6.0 API v2 対応版（Fabric）
- YetAnotherConfigLib v3（YACL）3.9.6+26.2-fabric 以降
- Mod Menu（任意。ゲーム内設定画面を使用する場合）

このMODはクライアント専用です。サーバーへの導入は不要です。

## 機能

- バニラが受信済みの Locator Bar waypoint だけを利用します。HUDの読み取りや、サーバーの可視性・受信距離制限の回避は行いません。
- JourneyMap の標準プレイヤーレーダーが同じプレイヤーを描画している間は、本MODの重複マーカーを表示しません。
- 標準レーダーが描画しない位置では、Locator Bar から得た座標をマーカーとして表示します。
- Locator Bar waypoint が取り消された場合、切断時、または設定をオフにした場合は、MODのマーカーを削除します。

## 設定

Mod Menu の **Player Radar Mod** → **設定** から `Show Locator Bar players` を切り替えられます。

設定は `config/player-radar-mod.json` に保存されます。Mod Menu を使用しない場合も、ゲームを終了してから次の内容を作成・編集することで切り替えられます。

```json
{
  "overlayEnabled": true
}
```

## 注意事項
> [!CAUTION]
> Locator Bar がクライアントへ座標を送らず、方角だけを送る状態では、クライアントMOD単体ではJourneyMap上の正確な位置を復元できません。特にPaper系サーバーの設定、遠距離テレポート、次元移動後などでこの状態になることがあります。この制限を解消するには、座標をクライアントへ送るサーバー側の仕組みが必要です。