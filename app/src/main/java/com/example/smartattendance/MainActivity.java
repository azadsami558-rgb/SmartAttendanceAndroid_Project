package com.example.smartattendance;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.net.Uri;
import android.graphics.Typeface;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    private JSONObject db;
    private LinearLayout root, content;
    private String language = "ar";
    private static final int EXPORT_CODE=301, IMPORT_CODE=302;
    private String today() { return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()); }
    private boolean ku(){ return language.equals("ku"); }
    private String tr(String key) {
        String[][] t = {
            {"title","نظام الحضور الذكي","سیستەمی ئامادەبوونی زیرەک"},
            {"home","الرئيسية","سەرەکی"},{"students","الطلاب","قوتابیان"},{"attendance","الحضور","ئامادەبوون"},
            {"reports","التقارير","ڕاپۆرتەکان"},{"warnings","الإنذارات","ئاگادارییەکان"},{"backup","نسخ احتياطي","پاشەکەوت"},
            {"add","إضافة طالب","زیادکردنی قوتابی"},{"name","اسم الطالب","ناوی قوتابی"},{"number","رقم الطالب","ژمارەی قوتابی"},
            {"class","الصف","پۆل"},{"save","حفظ","پاشەکەوتکردن"},{"present","حاضر","ئامادە"},
            {"late","متأخر","دواکەوتوو"},{"excused","غياب بعذر","نەهاتن بە بیانوو"},{"absent","غياب دون عذر","نەهاتن بێ بیانوو"},
            {"mark","تسجيل الحالة","تۆمارکردنی دۆخ"},{"total","إجمالي الطلاب","کۆی قوتابیان"},
            {"records","سجلات الحضور","تۆمارەکانی ئامادەبوون"},{"export","تصدير نسخة احتياطية","هەناردەکردنی پاشەکەوت"},
            {"import","استعادة نسخة احتياطية","گەڕاندنەوەی پاشەکەوت"},{"confirm","هل تريد استبدال البيانات الحالية؟","دەتەوێت داتاکانی ئێستا بگۆڕیت؟"},
            {"empty","لا توجد بيانات","هیچ داتایەک نییە"},{"no","لم يتم العثور على طالب","قوتابی نەدۆزرایەوە"},
            {"choose","اختر الحالة","دۆخ هەڵبژێرە"},{"date","تاريخ اليوم","بەرواری ئەمڕۆ"},
            {"search","ابحث بالاسم أو الرقم","بگەڕێ بە ناو یان ژمارە"},{"threshold","حدود الإنذار: الأول 3، الثاني 5، الحرمان 7","سنووری ئاگاداری: یەکەم ٣، دووەم ٥، بێبەشکردن ٧"},
            {"close","إغلاق","داخستن"},{"added","تمت إضافة الطالب","قوتابی زیادکرا"},
            {"missing","أكمل اسم الطالب ورقمه","ناو و ژمارەی قوتابی تەواو بکە"},{"saved","تم حفظ الحالة","دۆخەکە پاشەکەوت کرا"},
            {"restore","تمت استعادة النسخة الاحتياطية","پاشەکەوتەکە گەڕێندرایەوە"},
            {"backuphint","البيانات تحفظ على هذا الهاتف فقط. احتفظ بنسخة احتياطية خارج الهاتف عند الحاجة.","داتاکان تەنها لەم مۆبایلەدا دەپارێزرێن. کاتێک پێویست بوو پاشەکەوتێک لە دەرەوە هەڵبگرە."},
            {"student","طالب","قوتابی"},{"absentCount","غياب دون عذر","نەهاتن بێ بیانوو"},{"lateCount","مرات التأخير","ژمارەی دواکەوتن"},
            {"all","الكل","هەمووی"},{"classPick","اختر الصف","پۆل هەڵبژێرە"},{"done","تم","تەواو"}
        };
        for(String[] row:t) if(row[0].equals(key)) return ku()?row[2]:row[1];
        return key;
    }
    @Override public void onCreate(Bundle b) {
        super.onCreate(b); loadData(); showShell("home");
    }
    private void loadData() {
        try {
            File f=new File(getFilesDir(),"attendance.json");
            if(f.exists()) db=new JSONObject(readFile(new FileInputStream(f)));
            else { db=new JSONObject(readFile(getAssets().open("attendance_backup.json"))); saveData(); }
            if(!db.has("students")) db.put("students",new JSONArray());
            if(!db.has("classes")) db.put("classes",new JSONArray());
            if(!db.has("records")) db.put("records",new JSONArray());
            if(!db.has("settings")) db.put("settings",new JSONObject());
        } catch(Exception e) { db=new JSONObject(); try{db.put("students",new JSONArray());db.put("classes",new JSONArray());db.put("records",new JSONArray());db.put("settings",new JSONObject());}catch(Exception ignored){} }
    }
    private String readFile(InputStream in)throws Exception {
        ByteArrayOutputStream out=new ByteArrayOutputStream(); byte[] buf=new byte[8192]; int n;
        while((n=in.read(buf))>0) out.write(buf,0,n);
        in.close(); return new String(out.toByteArray(),StandardCharsets.UTF_8);
    }
    private void saveData() {
        try(FileOutputStream out=openFileOutput("attendance.json",MODE_PRIVATE)) { out.write(db.toString(2).getBytes(StandardCharsets.UTF_8)); }
        catch(Exception e) { toast("تعذر حفظ البيانات"); }
    }
    private JSONArray arr(String k){ JSONArray a=db.optJSONArray(k); return a==null?new JSONArray():a; }
    private void showShell(String page) {
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(14,12,14,8); root.setBackgroundColor(0xFFF4F6FA); setContentView(root);
        LinearLayout header=new LinearLayout(this); header.setOrientation(LinearLayout.HORIZONTAL); header.setGravity(Gravity.CENTER_VERTICAL);
        TextView title=txt(tr("title"),21,true); header.addView(title,new LinearLayout.LayoutParams(0,-2,1));
        Button lang=button(ku()?"العربية":"کوردی"); lang.setOnClickListener(v->{language=ku()?"ar":"ku";showShell(page);}); header.addView(lang); root.addView(header);
        TextView date=txt(today(),13,false); date.setTextColor(0xFF64748B); root.addView(date);
        LinearLayout nav=new LinearLayout(this); nav.setOrientation(LinearLayout.HORIZONTAL); nav.setPadding(0,8,0,8);
        String[][] tabs={{"home","⌂"},{"students","♙"},{"attendance","✓"},{"reports","▤"},{"warnings","!"},{"backup","⇧"}};
        for(String[] tab:tabs){ Button b=button(tr(tab[0])); b.setTextSize(10); b.setPadding(2,5,2,5); b.setOnClickListener(v->showShell(tab[0])); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1);p.setMargins(1,0,1,0);nav.addView(b,p);}
        root.addView(nav);
        ScrollView scroll=new ScrollView(this); content=new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(0,4,0,20); scroll.addView(content); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        switch(page){case "students":studentsPage();break;case "attendance":attendancePage();break;case "reports":reportsPage();break;case "warnings":warningsPage();break;case "backup":backupPage();break;default:homePage();}
    }
    private TextView txt(String s,int size,boolean bold) { TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(0xFF172033); if(bold)t.setTypeface(null,Typeface.BOLD); t.setPadding(4,7,4,7); return t; }
    private Button button(String s) { Button b=new Button(this); b.setText(s); b.setAllCaps(false); return b; }
    private void addText(String s,int size,boolean bold){content.addView(txt(s,size,bold));}
    private void addButton(String s,Runnable action){Button b=button(s); b.setOnClickListener(v->action.run()); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,4,0,4);content.addView(b,p);}
    private void card(String text){TextView t=txt(text,15,true); t.setBackgroundColor(0xFFFFFFFF); t.setPadding(14,12,14,12); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,4,0,4);content.addView(t,p);}
    private JSONObject studentById(String id){JSONArray a=arr("students");for(int i=0;i<a.length();i++){JSONObject s=a.optJSONObject(i);if(s!=null&&id.equals(s.optString("id")))return s;}return null;}
    private String studentName(String id){JSONObject s=studentById(id);return s==null?id:s.optString("name",id);}
    private int countToday(String status){int n=0;JSONArray a=arr("records");for(int i=0;i<a.length();i++){JSONObject r=a.optJSONObject(i);if(r!=null&&today().equals(r.optString("date"))&&status.equals(r.optString("status")))n++;}return n;}
    private void homePage(){
        addText(db.optJSONObject("settings")==null?"":db.optJSONObject("settings").optString("schoolName",""),16,true);
        card(tr("total")+": "+arr("students").length());
        card(tr("present")+": "+countToday("present"));
        card(tr("late")+": "+countToday("late"));
        card(tr("excused")+": "+countToday("absent_excused"));
        card(tr("absent")+": "+countToday("absent_unexcused"));
        addButton(tr("attendance"),()->showShell("attendance"));
        addButton(tr("students"),()->showShell("students"));
        addButton(tr("reports"),()->showShell("reports"));
    }
    private void studentsPage(){
        addButton("+ "+tr("add"),()->addStudentDialog());
        EditText search=new EditText(this);search.setSingleLine(true);search.setHint(tr("search"));content.addView(search);
        LinearLayout list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);content.addView(list);
        Runnable render=()->{list.removeAllViews();String q=search.getText().toString().trim().toLowerCase(Locale.ROOT);JSONArray a=arr("students");
            for(int i=0;i<a.length();i++){JSONObject s=a.optJSONObject(i);if(s==null)continue;String n=s.optString("name"),num=s.optString("studentNumber");if(!q.isEmpty()&&!n.toLowerCase(Locale.ROOT).contains(q)&&!num.toLowerCase(Locale.ROOT).contains(q))continue;
                Button b=button(n+"\n"+num+"  •  "+s.optString("className",""));b.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);b.setOnClickListener(v->studentDetail(s));list.addView(b,new LinearLayout.LayoutParams(-1,-2));}
        };
        search.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int before,int count){render.run();}public void afterTextChanged(android.text.Editable e){}});
        render.run();
    }
    private void studentDetail(JSONObject s){
        String id=s.optString("id");int abs=0,late=0;JSONArray a=arr("records");
        for(int i=0;i<a.length();i++){JSONObject r=a.optJSONObject(i);if(r!=null&&id.equals(r.optString("studentId"))){if("absent_unexcused".equals(r.optString("status")))abs++;if("late".equals(r.optString("status")))late++;}}
        new AlertDialog.Builder(this).setTitle(s.optString("name")).setMessage(tr("number")+": "+s.optString("studentNumber")+"\n"+tr("class")+": "+s.optString("className")+"\n"+tr("absentCount")+": "+abs+"\n"+tr("lateCount")+": "+late+"\n"+s.optString("notes","")).setPositiveButton(tr("close"),null).show();
    }
    private void addStudentDialog(){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(20,5,20,5);
        EditText name=new EditText(this);name.setHint(tr("name"));box.addView(name);
        EditText number=new EditText(this);number.setHint(tr("number"));box.addView(number);
        EditText className=new EditText(this);className.setHint(tr("class"));box.addView(className);
        new AlertDialog.Builder(this).setTitle(tr("add")).setView(box).setNegativeButton(tr("close"),null).setPositiveButton(tr("save"),(d,w)->{
            String n=name.getText().toString().trim(),num=number.getText().toString().trim(),cl=className.getText().toString().trim();
            if(n.isEmpty()||num.isEmpty()){toast(tr("missing"));return;}
            try{JSONObject s=new JSONObject();s.put("id","std-"+System.currentTimeMillis());s.put("studentNumber",num);s.put("name",n);s.put("className",cl);s.put("classId","custom");arr("students").put(s);saveData();toast(tr("added"));showShell("students");}catch(Exception e){toast("Error");}
        }).show();
    }
    private void attendancePage(){
        addText(tr("date")+": "+today(),16,true);
        EditText search=new EditText(this);search.setSingleLine(true);search.setHint(tr("search"));content.addView(search);
        LinearLayout list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);content.addView(list);
        Runnable render=()->{list.removeAllViews();String q=search.getText().toString().trim().toLowerCase(Locale.ROOT);JSONArray a=arr("students");
            for(int i=0;i<a.length();i++){JSONObject s=a.optJSONObject(i);if(s==null)continue;String n=s.optString("name"),num=s.optString("studentNumber");if(!q.isEmpty()&&!n.toLowerCase(Locale.ROOT).contains(q)&&!num.toLowerCase(Locale.ROOT).contains(q))continue;
                Button b=button(n+"\n"+num);b.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);b.setOnClickListener(v->markAttendance(s));list.addView(b,new LinearLayout.LayoutParams(-1,-2));}
        };
        search.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int before,int count){render.run();}public void afterTextChanged(android.text.Editable e){}});
        render.run();
    }
    private void markAttendance(JSONObject s){
        String[] labels={tr("present"),tr("late"),tr("excused"),tr("absent")};
        String[] statuses={"present","late","absent_excused","absent_unexcused"};
        new AlertDialog.Builder(this).setTitle(s.optString("name")+"\n"+tr("choose")).setItems(labels,(d,which)->{
            try{
                JSONArray a=arr("records");JSONObject found=null;
                for(int i=0;i<a.length();i++){JSONObject r=a.optJSONObject(i);if(r!=null&&s.optString("id").equals(r.optString("studentId"))&&today().equals(r.optString("date"))){found=r;break;}}
                if(found==null){found=new JSONObject();found.put("id","att-"+System.currentTimeMillis());found.put("studentId",s.optString("id"));found.put("date",today());a.put(found);}
                found.put("status",statuses[which]);found.put("timestamp",new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",Locale.US).format(new Date()));saveData();toast(tr("saved"));showShell("attendance");
            }catch(Exception e){toast("Error");}
        }).setNegativeButton(tr("close"),null).show();
    }
    private void reportsPage(){
        addText(tr("records")+" — "+arr("records").length(),18,true);
        addText(tr("date")+": "+today(),15,true);
        String[] sts={"present","late","absent_excused","absent_unexcused"};
        for(String st:sts){int n=countToday(st);String label=st.equals("present")?tr("present"):st.equals("late")?tr("late"):st.equals("absent_excused")?tr("excused"):tr("absent");card(label+": "+n);}
        addText("آخر السجلات / دواين تۆمارەکان",16,true);
        JSONArray a=arr("records");int start=Math.max(0,a.length()-30);
        for(int i=a.length()-1;i>=start;i--){JSONObject r=a.optJSONObject(i);if(r!=null)card(r.optString("date")+" • "+studentName(r.optString("studentId"))+" • "+statusLabel(r.optString("status")));}
    }
    private String statusLabel(String s){if(s.equals("present"))return tr("present");if(s.equals("late"))return tr("late");if(s.equals("absent_excused"))return tr("excused");if(s.equals("absent_unexcused"))return tr("absent");return s;}
    private void warningsPage(){
        addText(tr("threshold"),16,true);
        JSONArray students=arr("students");int warnings=0;
        for(int i=0;i<students.length();i++){JSONObject s=students.optJSONObject(i);if(s==null)continue;int abs=0;JSONArray rec=arr("records");for(int j=0;j<rec.length();j++){JSONObject r=rec.optJSONObject(j);if(r!=null&&s.optString("id").equals(r.optString("studentId"))&&"absent_unexcused".equals(r.optString("status")))abs++;}
            if(abs>=3){warnings++;String level=abs>=7?"حرمان / بێبەشکردن":abs>=5?"الإنذار الثاني / ئاگاداری دووەم":"الإنذار الأول / ئاگاداری یەکەم";card(s.optString("name")+"\n"+tr("absentCount")+": "+abs+"\n"+level);}
        }
        if(warnings==0)addText(tr("empty"),15,false);
        addText("ملاحظة: يتم حساب الحدود من سجلات الغياب دون عذر الموجودة فقط.",12,false);
    }
    private void backupPage(){
        addText(tr("backuphint"),15,false);
        addButton(tr("export"),()->{Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/json");i.putExtra(Intent.EXTRA_TITLE,"smart-attendance-backup.json");startActivityForResult(i,EXPORT_CODE);});
        addButton(tr("import"),()->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/json");startActivityForResult(i,IMPORT_CODE);});
        addText("النسخة الاحتياطية تشمل بيانات الطلاب والصفوف وسجلات الحضور والإعدادات.",13,false);
    }
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent dataIntent){
        super.onActivityResult(requestCode,resultCode,dataIntent);
        if(resultCode!=RESULT_OK||dataIntent==null||dataIntent.getData()==null)return;Uri uri=dataIntent.getData();
        try{
            if(requestCode==EXPORT_CODE){OutputStream out=getContentResolver().openOutputStream(uri);if(out==null)throw new IOException("Cannot open");out.write(db.toString(2).getBytes(StandardCharsets.UTF_8));out.close();toast("تم تصدير النسخة الاحتياطية");}
            else if(requestCode==IMPORT_CODE){new AlertDialog.Builder(this).setMessage(tr("confirm")).setNegativeButton(tr("close"),null).setPositiveButton(tr("done"),(d,w)->{try{InputStream in=getContentResolver().openInputStream(uri);JSONObject next=new JSONObject(readFile(in));if(!next.has("students")||!next.has("records"))throw new Exception("Invalid backup");db=next;saveData();showShell("home");toast(tr("restore"));}catch(Exception e){toast("ملف النسخة الاحتياطية غير صالح");}}).show();}
        }catch(Exception e){toast("تعذر تنفيذ العملية");}
    }
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
}
