"""Envia la version a la instancia existente, sin SSH ni datos privados en los comandos."""
import json, os, re, subprocess, time

def aws(*args):
    result = subprocess.run(['aws', *args, '--region', 'us-east-1', '--output', 'json'], text=True, capture_output=True)
    if result.returncode:
        raise RuntimeError(result.stderr.strip())
    return json.loads(result.stdout) if result.stdout.strip() else {}

bucket=os.environ['ARTIFACT_BUCKET']
instance=os.environ['EC2_INSTANCE_ID']
revision=os.environ['GITHUB_SHA']
assert re.fullmatch(r'[a-z0-9][a-z0-9.-]+',bucket)
assert re.fullmatch(r'i-[a-f0-9]+',instance)
assert re.fullmatch(r'[a-f0-9]{40}',revision)
command=aws('ssm','send-command','--instance-ids',instance,'--document-name','AWS-RunShellScript',
    '--timeout-seconds','300','--parameters',json.dumps({'commands':[
        f'aws s3 cp s3://{bucket}/deploy/aplicar-version.sh /run/aplicar-version.sh --only-show-errors',
        f'bash /run/aplicar-version.sh {bucket} {revision}'
    ]}))['Command']['CommandId']
for attempt in range(100):
    time.sleep(5)
    try:
        state=aws('ssm','get-command-invocation','--command-id',command,'--instance-id',instance)
    except RuntimeError as error:
        if 'InvocationDoesNotExist' in str(error): continue
        raise
    if state['Status']=='Success':
        print(state.get('StandardOutputContent','')); break
    if state['Status'] in ['Failed','Cancelled','TimedOut']:
        raise RuntimeError(state.get('StandardErrorContent','')+'\n'+state.get('StandardOutputContent',''))
else:
    raise TimeoutError('Systems Manager no completo el despliegue dentro del plazo.')
