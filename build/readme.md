#Build app

## Steps for e(fx)build
Note: in case of library modifications
1. Put correct "Application Version" in build.fxbuild
2. Generate Ant build.xml only

## Ant script
If not generated by previous step → compare your `build.xml` with `buildPMMSample.xml`.
Only path to librairies should be differents

Else, copy and replace content of `<target name="do-deploy" depends="setup-staging-area, do-compile, init-fx-tasks">` in your `build.xml`

Modify line `<attribute name="Implementation-Version" value="X.X.X"/>` with correct build version of PMM