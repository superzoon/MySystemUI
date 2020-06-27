#version 300 es
precision mediump float;
uniform samplerCube sTexture;//������������
in vec3 eyeVary;		//���մӶ�����ɫ����������������
in vec3 newNormalVary;	//���մӶ�����ɫ�������ı任������
out vec4 fragColor;//�������ƬԪ��ɫ
vec4 zfs( //�����۷������������ɫ�ķ���
  in float zsl//������
){  
  vec3 vTextureCoord;	//���ڽ�������ͼ�������������
  vec4 finalColorZS;		//��������Ĳ������
  vec4 finalColorFS;		//���Ƿ���Ĳ������
  vec4 finalColor;  		//������ɫ
  const float maxH=0.7;	//���������ֵ�����ڴ�ֵ�����������
  const float minH=0.2;	//���������ֵ��С�ڴ�ֵ������㷴��
  float sizeH=maxH-minH;//���ʱ����ֵ�Ŀ�� 
  float testValue=abs(dot(eyeVary,newNormalVary));	//�������������뷨����������ֵ
  if(testValue>maxH)  {							//����ֵ����maxH������
     vTextureCoord=refract(-eyeVary,newNormalVary,zsl);
     finalColor=texture(sTexture, vTextureCoord);  
  }  else if(testValue<=maxH&&testValue>=minH) {//����ֵ��minH��maxH��Χ�ڷ��䡢�����ں�
     vTextureCoord=reflect(-eyeVary,newNormalVary);
     finalColorFS=texture(sTexture, vTextureCoord);  	//����ļ�����
     vTextureCoord=refract(-eyeVary,newNormalVary,zsl);
     finalColorZS=texture(sTexture, vTextureCoord);  	//����ļ�����
     float ratio=(testValue-minH)/sizeH;					//�ںϱ���
     finalColor=finalColorZS*ratio+(1.0-ratio)*finalColorFS;	//�۷����������ں�
  }  else{										//����ֵС��minH������
     vTextureCoord=reflect(-eyeVary,newNormalVary);
     finalColor=texture(sTexture, vTextureCoord);  
  }  
  return finalColor;									//�������ս��
}
void main(){
   vec4 finalColor=vec4(0.0,0.0,0.0,0.0);
   //������ɫɢRGB����ɫ��ͨ�����������۷���
   finalColor.r=zfs(0.97).r;  		//�����ɫͨ��
   finalColor.g=zfs(0.955).g;  		//������ɫͨ��
   finalColor.b=zfs(0.94).b;  		//������ɫͨ��
   fragColor=finalColor; 		//�����յ�ƬԪ��ɫ���ݸ�����
} 
