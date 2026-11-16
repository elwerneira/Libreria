from pathlib import Path
import re
root=Path(__file__).resolve().parents[1]
dest=root/'arquetipo-libros/src/main/resources/archetype-resources'
# Lista explicita: nunca copiar wallets, config privada, .git o target a la plantilla.
paths=[]
for directory in ['src/main/java','src/test/java','src/main/resources','src/test/resources','sql']:
    paths.extend(p for p in (root/directory).rglob('*') if p.is_file())
paths.extend(root/p for p in ['config/application-local.example.properties','.mvn/wrapper/maven-wrapper.properties','mvnw','mvnw.cmd','.gitignore','.gitattributes'])
for source in paths:
    relative=source.relative_to(root)
    data=source.read_text(encoding='utf-8-sig')
    if source.suffix=='.java':
        if 'main/java' in relative.as_posix():
            relative=Path('src/main/java')/source.relative_to(root/'src/main/java/Gestionar/Libros')
        else:
            relative=Path('src/test/java')/source.relative_to(root/'src/test/java/Gestionar/Libros')
        data=data.replace('Gestionar.Libros','${package}')
    target=dest/relative
    target.parent.mkdir(parents=True,exist_ok=True)
    target.write_text(data,encoding='utf-8',newline='\n')
pom=(root/'pom.xml').read_text(encoding='utf-8-sig')
pom=pom.replace('<groupId>Gestionar</groupId>','<groupId>${groupId}</groupId>',1)
pom=pom.replace('<artifactId>Libros</artifactId>','<artifactId>${artifactId}</artifactId>',1)
pom=pom.replace('<version>0.0.1-SNAPSHOT</version>','<version>${version}</version>',1)
pom=pom.replace('${java.version}',"${symbol_dollar}{java.version}")
pom='#set( $symbol_dollar = \'$\' )\n'+pom
(dest/'pom.xml').write_text(pom,encoding='utf-8',newline='\n')
assert not any('Wallet' in str(p) or p.name=='application-local.properties' for p in dest.rglob('*'))
assert '${package}' in (dest/'src/main/java/model/Libro.java').read_text(encoding='utf-8')
print('Plantilla preparada:',len(paths)+1,'archivos; sin wallets ni credenciales.')
