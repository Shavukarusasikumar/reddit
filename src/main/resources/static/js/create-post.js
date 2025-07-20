(function(){
  const communityBtn   = document.getElementById('communityPickerBtn');
  const communityMenu  = document.getElementById('communityPickerMenu');
  const communityList  = document.getElementById('communityList');
  const communityLabel = document.getElementById('communityPickerLabel');
  const communityIdInp = document.getElementById('communityIdInput');

  const flairSelect    = document.getElementById('flairSelect');
  const flairIdInp     = document.getElementById('flairIdInput');

  const tabs   = document.querySelectorAll('.cp-tab');
  const panels = {TEXT:document.getElementById('panel-text'), MEDIA:document.getElementById('panel-media')};
  const postTypeInp = document.getElementById('postTypeInput');

  const titleInput = document.getElementById('titleInput');
  const titleChars = document.getElementById('titleChars');
  const postBtn    = document.getElementById('postBtn');
  const saveBtn    = document.getElementById('saveDraftBtn');

  // Toggle menu
  communityBtn?.addEventListener('click',()=>{communityMenu.hidden=!communityMenu.hidden;});
  document.addEventListener('click',(e)=>{if(!communityMenu.contains(e.target)&&!communityBtn.contains(e.target)){communityMenu.hidden=true;}});

  // Pick community -> reload page with ?c=<id>
  communityList?.addEventListener('click',e=>{
    const li=e.target.closest('.cp-community-item');
    if(!li)return;
    const id=li.dataset.id; const name=li.dataset.name;
    communityLabel.textContent=name;
    communityIdInp.value=id;
    communityMenu.hidden=true;
    // full page reload to fetch flairs server-side
    window.location.href = '/new-post?c=' + encodeURIComponent(id);
  });

  // Filter
  document.getElementById('communitySearchInput')?.addEventListener('input',function(){
    const q=this.value.toLowerCase();
    communityList.querySelectorAll('.cp-community-item').forEach(li=>{
      const name=li.dataset.name?.toLowerCase()??''; li.style.display=name.includes(q)?'':'none';
    });
  });

  // Tabs
  tabs.forEach(tab=>tab.addEventListener('click',()=>{
    const type=tab.dataset.type; postTypeInp.value=type;
    tabs.forEach(t=>t.classList.remove('cp-tab-active')); tab.classList.add('cp-tab-active');
    Object.keys(panels).forEach(k=>panels[k].hidden=(k!==type));
    validate();
  }));

  // Validate
  function validate(){
    const hasTitle=titleInput.value.trim().length>0;
    const hasCommunity=communityIdInp.value!=='';
    let hasContent=true;
    if(postTypeInp.value==='MEDIA'){
      const fileInput=document.getElementById('mediaFileInput');
      hasContent=fileInput&&fileInput.files&&fileInput.files.length>0;
    }
    const valid=hasTitle&&hasCommunity&&hasContent;
    postBtn.disabled=!valid; postBtn.classList.toggle('enabled',valid);
    saveBtn.disabled=!hasTitle; saveBtn.classList.toggle('enabled',hasTitle);
  }
  titleInput?.addEventListener('input',()=>{titleChars.textContent=titleInput.value.length; validate();});
  document.addEventListener('DOMContentLoaded',validate);

  // Flair select -> copy to hidden
  flairSelect?.addEventListener('change',()=>{flairIdInp.value=flairSelect.value||'';});

  // MEDIA dropzone preview
  const dropzone=document.getElementById('mediaDropzone');
  const fileInput=document.getElementById('mediaFileInput');
  const preview=document.getElementById('mediaPreview');
  if(dropzone){
    dropzone.addEventListener('click',()=>fileInput.click());
    dropzone.addEventListener('dragover',e=>{e.preventDefault(); dropzone.classList.add('cp-dragover');});
    dropzone.addEventListener('dragleave',()=>dropzone.classList.remove('cp-dragover'));
    dropzone.addEventListener('drop',e=>{e.preventDefault(); dropzone.classList.remove('cp-dragover'); fileInput.files=e.dataTransfer.files; previewFromList(e.dataTransfer.files); validate();});
    fileInput?.addEventListener('change',e=>{previewFromList(e.target.files); validate();});
  }
  function previewFromList(fileList){
    preview.innerHTML='';
    [...fileList].forEach(f=>{const url=URL.createObjectURL(f); let el; if(f.type.startsWith('image/')){el=document.createElement('img'); el.src=url;} else if(f.type.startsWith('video/')){el=document.createElement('video'); el.src=url; el.controls=true;} else {return;} preview.appendChild(el);});
  }
})();

