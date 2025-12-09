<table align="center">
  <tr>
    <td style="vertical-align: middle;">
      <!-- GIF di presentazione, metti qui quella che preferisci -->
      <img src="https://media.tenor.com/zl0hgjC3_cgAAAAC/hello.gif" width="350" alt="VersionConnector Animation">
    </td>
    <td style="padding-left: 24px; vertical-align: middle;">
      <samp>
        <strong>VersionConnector</strong> – Multi-Version Bungee Plugin<br>
        <small>
          Fork Version: <code>2.0.0</code> | Minecraft <code>1.8 ~ 1.21+</code>
        </small><br><br>
        <img alt="Java" src="https://img.shields.io/badge/-Java-blue?style=for-the-badge&logo=java&logoColor=white">
        <img alt="BungeeCord" src="https://img.shields.io/badge/-BungeeCord-orange?style=for-the-badge&logo=apachekafka&logoColor=white">
        <img alt="Minecraft" src="https://img.shields.io/badge/-1.21+_Supported-green?style=for-the-badge&logo=minecraft&logoColor=white">
        <br><br>
        <span style="color:#ff4444;"><strong>⚠️ WARNING:</strong></span>
        This is a <b>fork</b> of the original VersionConnector plugin<br>
        with added support for newer Minecraft versions <b>(from 1.21 onwards)</b>.
        <br><br>
        Connect different Minecraft client versions to dedicated servers.<br>
        Includes automatic version-based routing, simple load-balancing,<br>
        and Forge switch support.<br>
        <br>
        <a href="http://ci.minebench.de/job/VersionConnector/" target="_blank">
          <strong>Development builds &rarr; Minebench Jenkins</strong>
        </a>
      </samp>
    </td>
  </tr>
</table>
<br>

---

## Supported Versions

VersionConnector uses easy constants for version-based routing.  
See [ProtocolVersion.java](https://github.com/Minebench/VersionConnector/blob/master/src/main/java/de/themoep/versionconnector/ProtocolVersion.java) for full details.

It will fallback to the closest protocol below the actual client version, or you can set protocol versions directly.

---

## Example Config

```yaml
debug: false
start-balancing: 0
join:
  lobby:
    versions:
      '34': lobby_prot_34
      '1_8': lobby_1_8_a, lobby_1_8_b
      '1_9': lobby_1_9
      UNKNOWN: well_we_dont_know
    forge:
      '1_9': forge_lobby_1_9
      '1_8': forge_lobby_1_8_a, forge_lobby_1_8_b
    mods:
      "modname1,modname2": mod_server
servers:
  survival:
    versions:
      '1_8': survival_1_8
      '1_10': survival_1_10
      UNKNOWN: survival_wat
    forge:
      '1_9': forge_suvival_1_9
      '1_8': forge_suvival_1_8_a, forge_suvival_1_8_b
    mods:
      "modname1,modname2": mod_server
```

---

## Features

- Route players based on Minecraft version (supports 1.8–1.21+)
- Simple load balancing across lobbies and servers
- Detect and redirect Forge clients (1.8–1.13)
- Mod-based routing
- Easy YAML configuration

---

For plugin updates, issues, and community join:
- [Minebench Jenkins](http://ci.minebench.de/job/VersionConnector/)
- [Repository](https://github.com/LorenzoZ-DEV/VersionConnector)
