[![Showcase](https://img.shields.io/badge/Showcase-1A1C20?style=for-the-badge&logo=youtube&logoColor=FF0000)](https://youtu.be/lF4euVOg3-k)
[![Mod Documentation](https://img.shields.io/badge/Mod_Documentation-1A1C20?style=for-the-badge&logo=github&logoColor=FFFFFF)](https://github.com/be-bub/genderbub/wiki/Mod)
[![Resource Pack Documentation](https://img.shields.io/badge/Resource_Pack_Documentation-1A1C20?style=for-the-badge&logo=github&logoColor=FFFFFF)](https://github.com/be-bub/genderbub/wiki/Resource-Pack)
[![Compatibility](https://img.shields.io/badge/Compatibility-1A1C20?style=for-the-badge&logo=github&logoColor=FFFFFF)](https://github.com/be-bub/genderbub/wiki/Compatibility)

- **Random gender:** male or female on spawn with configurable chance (0-50% each)
- **Sterility:** infertile mobs can't breed, chance auto-calculated from gender settings
- **Visual icons:** configurable gender icon above mobs and HUD when holding scanner
- **Breeding control:** block same-gender breeding, disable sterile breeding, or allow all
- **Item restrictions:** prevent specific items on certain genders (e.g., milking male cows)
- **Villager support:** villagers get genders, preserved when curing zombie villagers
- **Modded mob support:** auto-scan or manually add any modded animal to config

[![CurseForge](https://img.shields.io/curseforge/dt/1497835?style=for-the-badge&logo=curseforge&logoColor=F16436&label=CurseForge&labelColor=1A1C20&color=2f353d)](https://curseforge.com/minecraft/mc-mods/genderbub)
[![Modrinth](https://img.shields.io/modrinth/dt/genderbub?style=for-the-badge&logo=modrinth&logoColor=00AF5C&label=Modrinth&labelColor=1A1C20&color=2f353d)](https://modrinth.com/mod/genderbub)
[![Config Editor](https://img.shields.io/badge/Config_Editor-1A1C20?style=for-the-badge&logo=github&logoColor=FFFFFF)](https://be-bub.github.io/genderbub/)

<table style="width: 100%; border: none; background-color: #1A1C20; border-radius: 8px; padding: 10px; margin-bottom: 16px; display: table;">
  <tr>
    <td style="border: none; padding: 12px; text-align: left; color: #e1e3e6; font-size: 24px; font-weight: bold;">
      Modpack?
    </td>
  </tr>
  <tr>
    <td style="border: none; padding: 12px; text-align: left; color: #b0b3bc; font-size: 14px; line-height: 1.6;">
      Yes, you can use this mod in your modpacks. The mod automatically scans animals on first launch or with the <code style="background-color: #2a2a30; padding: 2px 6px; border-radius: 4px; color: #e1e3e6;">/bub server scan</code> command, but some mobs may be missed. If a modded animal doesn't get a gender, add its ID manually to the <code style="background-color: #2a2a30; padding: 2px 6px; border-radius: 4px; color: #e1e3e6;">enabledMobs</code> list in the config. Don't hesitate to report any incompatibility issues with other mods. Always make sure to use the latest version of the mod.
    </td>
  </tr>
</table>

<table style="width: 100%; border: none; background-color: #1A1C20; border-radius: 8px; padding: 10px; margin-bottom: 16px; display: table;">
  <tr>
    <td style="border: none; padding: 12px; text-align: left; color: #e1e3e6; font-size: 24px; font-weight: bold;">
      Can I configure the mod for myself or a modpack?
    </td>
  </tr>
  <tr>
    <td style="border: none; padding: 12px; text-align: left; color: #b0b3bc; font-size: 14px; line-height: 1.6;">
      Yes, if you need to add integration with a mod that hasn't been configured yet, you can do it yourself. For example, if you want mob A to always spawn as gender B, go to the <a href="https://be-bub.github.io/genderbub/" style="color: #3b82f6; text-decoration: none;">Config Editor</a>, select "Integration Editor", and create a new integration. After that, place the generated files in <code style="background-color: #2a2a30; padding: 2px 6px; border-radius: 4px; color: #e1e3e6;">config/genderbub/integration/compa</code> and run the command <code style="background-color: #2a2a30; padding: 2px 6px; border-radius: 4px; color: #e1e3e6;">/bub server reload</code>.
    </td>
  </tr>
</table>

<table style="width: 100%; border: none; background-color: #1A1C20; border-radius: 8px; padding: 10px; margin-bottom: 16px; display: table;">
  <tr>
    <td style="border: none; padding: 12px; text-align: left; color: #e1e3e6; font-size: 24px; font-weight: bold;">
      Compatibility with other mods?
    </td>
  </tr>
  <tr>
    <td style="border: none; padding: 12px; text-align: left; color: #b0b3bc; font-size: 14px; line-height: 1.6;">
      Yes, the mod was designed to expand gameplay and covers mobs from other mods. However, some mods require manual configuration for correct gender distribution. For example, with the <strong>Naturalist</strong> mod, lions with manes are male and lions without manes are female. A list of mods that I have already configured can be found here: <a href="https://github.com/be-bub/genderbub/wiki/Compatibility" style="color: #3b82f6; text-decoration: none;">Compatibility</a>.
    </td>
  </tr>
</table>

<div style="color: #b0b3bc; font-size: 14px; margin-top: 20px; padding: 10px; text-align: left;">
  English, Русский, Українська, Deutsch, Español, Français, 日本語, 한국어, 简体中文, 繁體中文
  <br>
  <span style="color: #7c7f8c; font-size: 12px;">Commands: English only</span>
</div>
