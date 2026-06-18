[![Showcase](https://img.shields.io/badge/Showcase-1A1C20?style=for-the-badge)](https://youtu.be/lF4euVOg3-k)
[![Config Editor](https://img.shields.io/badge/Config_Editor-1A1C20?style=for-the-badge)](https://be-bub.github.io/genderbub/)
[![Mod Documentation](https://img.shields.io/badge/Mod_Documentation-1A1C20?style=for-the-badge)](https://github.com/be-bub/genderbub/wiki/Mod)
[![CurseForge](https://img.shields.io/curseforge/dt/1497835?style=for-the-badge&logo=curseforge&logoColor=F16436&label=&labelColor=1A1C20&color=2f353d)](https://curseforge.com/minecraft/mc-mods/genderbub)
[![Modrinth](https://img.shields.io/modrinth/dt/genderbub?style=for-the-badge&logo=modrinth&logoColor=00AF5C&label=&labelColor=1A1C20&color=2f353d)](https://modrinth.com/mod/genderbub)

<table style="width: 100%; border: 1px solid #2a2a30; background-color: #0d0d0d; border-radius: 8px; padding: 0; margin-bottom: 16px; border-collapse: collapse;">
  <tr>
    <td style="border: none; padding: 10px 14px 6px 14px; font-size: 18px; font-weight: 700; color: #b3b3b2; border-bottom: 1px solid #2a2a30; background-color: #1a1a1f; font-family: 'Roboto', sans-serif; border-radius: 8px 8px 0 0;">README</td>
  </tr>
  <tr>
    <td style="border: none; padding: 10px 14px 12px 14px; font-size: 14px; line-height: 1.6; color: #b3b3b2; background-color: #0d0d0d; font-family: 'Roboto', sans-serif; border-radius: 0 0 8px 8px;">
      This mod adds genders to mobs. Almost everything is configurable, and players can create their own integrations. Check the wiki before building a modpack. If you're adding the mod to an existing world, make a backup first. Thanks for your support!
    </td>
  </tr>
</table>

Each mob gets assigned a random gender upon spawning, with a configurable chance from 0% to 50% for each option. Infertile mobs cannot breed, and the probability is calculated automatically based on your male/female ratio settings. A configurable gender icon appears above mobs and in the HUD when holding the scanner item. You can block same-gender breeding, disable sterile breeding, or allow all without restrictions. Prevents using specific items on certain genders, such as milking a male cow. Villagers receive genders, and this is preserved when curing zombie villagers. Automatically scans for modded animals that extend the Animal class, or you can manually add any animal to the config.

<table style="width: 100%; border: none; background-color: #1A1C20; border-radius: 8px; padding: 10px; margin-bottom: 16px; display: table;">
  <tr>
    <td style="border: none; padding: 12px; text-align: left; font-size: 24px; font-weight: bold;">
      Can I use it in modpacks?
    </td>
  </tr>
  <tr>
    <td style="border: none; padding: 12px; text-align: left; font-size: 14px; line-height: 1.6;">
      Yes, you can use this mod in your modpacks without asking for permission or giving credit. The mod automatically scans animals on first launch or with the <code style="background-color: #2a2a30; padding: 2px 6px; border-radius: 4px;">/bub server scan</code> command, but some mobs may be missed. If a modded animal doesn't get a gender, add its ID manually to the <code style="background-color: #2a2a30; padding: 2px 6px; border-radius: 4px;">enabledMobs</code> list in the config (inside your Minecraft instance folder). Don't hesitate to report any incompatibility issues with other mods, and always make sure to use the latest version of the mod.
    </td>
  </tr>
</table>

<table style="width: 100%; border: none; background-color: #1A1C20; border-radius: 8px; padding: 10px; margin-bottom: 16px; display: table;">
  <tr>
    <td style="border: none; padding: 12px; text-align: left; font-size: 24px; font-weight: bold;">
      Can I set up integration myself?
    </td>
  </tr>
  <tr>
    <td style="border: none; padding: 12px; text-align: left; font-size: 14px; line-height: 1.6;">
      Yes, if you need to add integration with a mod that hasn't been configured yet, you can do it yourself. For example, if you want mob A to always spawn as gender B, go to the <a href="https://be-bub.github.io/genderbub/" style="text-decoration: none;">Config Editor</a>, select "Integration Editor", and create a new integration. After that, place the generated files in <code style="background-color: #2a2a30; padding: 2px 6px; border-radius: 4px;">config/genderbub/integration/compat</code> (inside your Minecraft instance folder) and run the command <code style="background-color: #2a2a30; padding: 2px 6px; border-radius: 4px;">/bub server integration</code>.
    </td>
  </tr>
</table>

<table style="width: 100%; border: none; background-color: #1A1C20; border-radius: 8px; padding: 10px; margin-bottom: 16px; display: table;">
  <tr>
    <td style="border: none; padding: 12px; text-align: left; font-size: 24px; font-weight: bold;">
      Compatibility with other mods?
    </td>
  </tr>
  <tr>
    <td style="border: none; padding: 12px; text-align: left; font-size: 14px; line-height: 1.6;">
      Yes, the mod was designed to expand gameplay and covers mobs from other mods. However, some mods require manual configuration for correct gender distribution. For example, with the <strong>Naturalist</strong> mod, lions with manes are male and lions without manes are female. A list of mods that I have already configured can be found here: <a href="https://github.com/be-bub/genderbub/wiki/Compatibility" style="text-decoration: none;">Compatibility</a>.
    </td>
  </tr>
</table>

<div style="color: #b0b3bc; font-size: 14px; margin-top: 20px; padding: 10px; text-align: left;">
  English, Русский, Українська, Deutsch, Español, Français, 日本語, 한국어, 简体中文, 繁體中文
  <br>
  <span style="color: #7c7f8c; font-size: 12px;">Commands: English only</span>
</div>
